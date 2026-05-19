package com.property.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ClaimRulesDTO {
    @NotEmpty
    private String projectId;
    @NotEmpty
    private List<RuleItem> rules;

    @Data
    public static class RuleItem {
        private String name;
        private Integer priority;
        private String conditionJson;
        private String actionJson;
        private Boolean enabled;
    }
}
