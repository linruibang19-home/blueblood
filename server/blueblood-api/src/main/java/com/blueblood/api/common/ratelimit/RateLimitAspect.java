package com.blueblood.api.common.ratelimit;

import com.blueblood.api.common.exception.BusinessException;
import com.blueblood.api.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * 限流切面:Redis Lua 实现"INCR + 首次 EXPIRE + 超限判定"原子计数(固定窗口)。
 * Lua 在 Redis 单线程内不可被打断,保证计数与过期设置的原子性。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final StringRedisTemplate redis;
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();

    private static final DefaultRedisScript<Long> SCRIPT;
    static {
        SCRIPT = new DefaultRedisScript<>();
        // ARGV[1]=窗口秒 ARGV[2]=最大次数;首次访问设置过期,超过次数返回 0
        SCRIPT.setScriptText(
                "local c = redis.call('INCR', KEYS[1]) " +
                "if tonumber(c) == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                "if tonumber(c) > tonumber(ARGV[2]) then return 0 else return 1 end");
        SCRIPT.setResultType(Long.class);
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        String key = "rate:" + sig.getDeclaringType().getSimpleName() + ":" + method.getName() + ":" + resolveKey(rateLimit, pjp);

        Long allowed = redis.execute(SCRIPT, Collections.singletonList(key),
                String.valueOf(rateLimit.seconds()), String.valueOf(rateLimit.count()));
        if (allowed == null || allowed == 0L) {
            throw new BusinessException(ResultCode.OPERATION_FAILED, rateLimit.message());
        }
        return pjp.proceed();
    }

    /** 解析限流 key:SpEL 表达式 → 具体值;为空则用客户端 IP。 */
    private String resolveKey(RateLimit rl, ProceedingJoinPoint pjp) {
        if (rl.key() == null || rl.key().isEmpty()) {
            return clientIp();
        }
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Object[] args = pjp.getArgs();
        EvaluationContext ctx = new StandardEvaluationContext();
        // 支持 #参数名
        String[] names = pnd.getParameterNames(method);
        if (names != null) {
            for (int i = 0; i < names.length && i < args.length; i++) {
                ctx.setVariable(names[i], args[i]);
            }
        }
        // 支持 #a0/#a1 索引(不依赖编译参数名保留)
        for (int i = 0; i < args.length; i++) {
            ctx.setVariable("a" + i, args[i]);
        }
        try {
            Expression exp = parser.parseExpression(rl.key());
            Object v = exp.getValue(ctx);
            return v == null ? "null" : v.toString();
        } catch (Exception e) {
            return "default";
        }
    }

    private String clientIp() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr == null) {
                return "unknown";
            }
            HttpServletRequest req = attr.getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty()) {
                ip = req.getRemoteAddr();
            }
            return ip == null ? "unknown" : ip;
        } catch (Exception e) {
            return "unknown";
        }
    }
}
