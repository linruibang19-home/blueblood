package com.blueblood.api.common.ratelimit;

import java.lang.annotation.*;

/**
 * 接口限流注解(基于 Redis + Lua 固定窗口)。
 * 切面 {@link RateLimitAspect} 拦截,计数原子,超限抛业务异常。
 *
 * <p>key 支持 SpEL:如 {@code #a0}(第一个参数)、{@code #a0.username};为空则用客户端 IP。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /** 窗口内允许次数,默认 1 */
    int count() default 1;

    /** 窗口秒数,默认 60 */
    int seconds() default 60;

    /** 限流 key 的 SpEL 表达式(如 "#a0"、"#a0.username");为空则用客户端 IP */
    String key() default "";

    /** 超限提示信息 */
    String message() default "操作过于频繁,请稍后再试";
}
