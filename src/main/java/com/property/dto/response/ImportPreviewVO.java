package com.property.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "Excel 导入预览结果")
public class ImportPreviewVO {

    @Schema(description = "校验通过的行数")
    private int successRows;

    @Schema(description = "校验失败的行数")
    private int failRows;

    @Schema(description = "失败行详情")
    private List<RowError> errors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "行级错误信息")
    public static class RowError {

        @Schema(description = "Excel 行号（从2开始，含表头）")
        private int row;

        @Schema(description = "错误原因")
        private String reason;
    }
}
