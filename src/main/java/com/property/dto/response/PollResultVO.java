package com.property.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class PollResultVO {
    private String pollId;
    private String title;
    private String type;
    private String status;
    private int totalVotes;
    private List<OptionResultVO> options;

    @Data
    public static class OptionResultVO {
        private String optionId;
        private String content;
        private int sortOrder;
        private int voteCount;
        private double percentage;
    }
}
