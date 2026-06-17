package com.blueblood.api.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页返回结构：{ list, total, page, pageSize, totalPages }
 */
@Data
@Schema(description = "分页返回结构")
public class PageResult<T> implements Serializable {

    @Schema(description = "数据列表")
    private List<T> list;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long page;

    @Schema(description = "每页条数")
    private long pageSize;

    @Schema(description = "总页数")
    private long totalPages;

    public PageResult() {
    }

    public PageResult(List<T> list, long total, long page, long pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (total + pageSize - 1) / pageSize : 0;
    }

    /**
     * 由 MyBatis-Plus 的 IPage 构造分页结果。
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        List<T> records = page.getRecords() != null ? page.getRecords() : Collections.emptyList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }
}
