package com.property.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class VoteDTO {
    private List<String> optionIds;
}
