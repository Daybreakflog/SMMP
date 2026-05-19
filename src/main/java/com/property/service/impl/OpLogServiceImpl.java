package com.property.service.impl;

import com.property.service.OpLogService;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.OpLog;
import com.property.mapper.OpLogMapper;
import com.property.dto.request.OpLogQueryDTO;
import com.property.dto.response.OpLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class OpLogServiceImpl implements OpLogService {

    private final OpLogMapper opLogMapper;

    public PageResult<OpLogVO> page(OpLogQueryDTO query) {
        LambdaQueryWrapper<OpLog> wrapper = new LambdaQueryWrapper<OpLog>()
                .eq(StrUtil.isNotBlank(query.getOperatorId()), OpLog::getActorId, query.getOperatorId())
                .eq(StrUtil.isNotBlank(query.getModule()), OpLog::getTarget, query.getModule())
                .ge(query.getStartDate() != null, OpLog::getAt,
                        query.getStartDate() != null ? query.getStartDate().atStartOfDay() : null)
                .le(query.getEndDate() != null, OpLog::getAt,
                        query.getEndDate() != null ? query.getEndDate().atTime(LocalTime.MAX) : null)
                .orderByDesc(OpLog::getAt);

        return PageResult.of(
                opLogMapper.selectPage(new Page<>(query.getPage(), query.getPageSize()), wrapper)
                           .convert(this::toVO)
        );
    }

    public OpLogVO getById(Long id) {
        OpLog entity = opLogMapper.selectById(id);
        if (entity == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        return toVO(entity);
    }

    // ── private ──────────────────────────────────────────────────────────────

    private OpLogVO toVO(OpLog e) {
        OpLogVO vo = new OpLogVO();
        vo.setId(e.getId());
        vo.setActorId(e.getActorId());
        vo.setActorName(e.getActorName());
        vo.setAction(e.getAction());
        vo.setTarget(e.getTarget());
        vo.setDiff(e.getDiff());
        vo.setIp(e.getIp());
        vo.setUserAgent(e.getUserAgent());
        vo.setTraceId(e.getTraceId());
        vo.setAt(e.getAt());
        return vo;
    }
}
