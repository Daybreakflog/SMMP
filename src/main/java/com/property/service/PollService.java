package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Poll;
import com.property.dto.request.PollDTO;
import com.property.dto.request.PollQueryDTO;
import com.property.dto.request.VoteDTO;
import com.property.dto.response.PollResultVO;
import com.property.dto.response.PollVO;

public interface PollService {

    PageResult<PollVO> page(PollQueryDTO query);

    PollVO getById(String id);

    PollVO create(PollDTO dto);

    PollVO update(String id, PollDTO dto);

    PollVO publish(String id);

    PollVO close(String id);

    void delete(String id);

    void vote(String id, VoteDTO dto);

    PollResultVO result(String id);
}
