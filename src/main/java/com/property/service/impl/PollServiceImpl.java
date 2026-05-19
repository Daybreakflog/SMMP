package com.property.service.impl;

import com.property.service.PollService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.Poll;
import com.property.entity.PollOption;
import com.property.entity.PollVote;
import com.property.mapper.PollMapper;
import com.property.mapper.PollOptionMapper;
import com.property.mapper.PollVoteMapper;
import com.property.dto.request.PollDTO;
import com.property.dto.request.PollQueryDTO;
import com.property.dto.request.VoteDTO;
import com.property.dto.response.PollOptionVO;
import com.property.dto.response.PollResultVO;
import com.property.dto.response.PollVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollServiceImpl implements PollService {

    private final PollMapper pollMapper;
    private final PollOptionMapper pollOptionMapper;
    private final PollVoteMapper pollVoteMapper;

    public PageResult<PollVO> page(PollQueryDTO query) {
        LambdaQueryWrapper<Poll> wrapper = new LambdaQueryWrapper<Poll>()
                .eq(StrUtil.isNotBlank(query.getStatus()), Poll::getStatus, query.getStatus())
                .orderByDesc(Poll::getCreatedAt);
        Page<Poll> raw = pollMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper);
        List<PollVO> vos = raw.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(vos, raw.getTotal(), raw.getCurrent(), raw.getSize());
    }

    public PollVO getById(String id) {
        Poll poll = requireExist(id);
        PollVO vo = toVO(poll);
        vo.setOptions(findOptions(id));
        return vo;
    }

    @Transactional
    public PollVO create(PollDTO dto) {
        Poll poll = new Poll();
        applyDTO(poll, dto);
        poll.setAuthorId(StpUtil.getLoginIdAsString());
        poll.setStatus("DRAFT");
        pollMapper.insert(poll);

        if (dto.getOptions() != null) {
            for (int i = 0; i < dto.getOptions().size(); i++) {
                PollDTO.PollOptionItem item = dto.getOptions().get(i);
                PollOption opt = new PollOption();
                opt.setPollId(poll.getId());
                opt.setContent(item.getContent());
                opt.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : i);
                pollOptionMapper.insert(opt);
            }
        }

        PollVO vo = toVO(poll);
        vo.setOptions(findOptions(poll.getId()));
        return vo;
    }

    @Transactional
    public PollVO update(String id, PollDTO dto) {
        Poll poll = requireExist(id);
        if (!"DRAFT".equals(poll.getStatus())) {
            throw new BusinessException(ErrorCode.POLL_STATUS_ILLEGAL);
        }
        applyDTO(poll, dto);
        pollMapper.updateById(poll);

        if (dto.getOptions() != null) {
            pollOptionMapper.delete(
                    new LambdaQueryWrapper<PollOption>().eq(PollOption::getPollId, id));
            for (int i = 0; i < dto.getOptions().size(); i++) {
                PollDTO.PollOptionItem item = dto.getOptions().get(i);
                PollOption opt = new PollOption();
                opt.setPollId(id);
                opt.setContent(item.getContent());
                opt.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : i);
                pollOptionMapper.insert(opt);
            }
        }

        PollVO vo = toVO(poll);
        vo.setOptions(findOptions(id));
        return vo;
    }

    @Transactional
    public PollVO publish(String id) {
        Poll poll = requireExist(id);
        if (!"DRAFT".equals(poll.getStatus())) {
            throw new BusinessException(ErrorCode.POLL_STATUS_ILLEGAL);
        }
        poll.setStatus("PUBLISHED");
        pollMapper.updateById(poll);
        PollVO vo = toVO(poll);
        vo.setOptions(findOptions(id));
        return vo;
    }

    @Transactional
    public PollVO close(String id) {
        Poll poll = requireExist(id);
        if (!"PUBLISHED".equals(poll.getStatus())) {
            throw new BusinessException(ErrorCode.POLL_STATUS_ILLEGAL);
        }
        poll.setStatus("CLOSED");
        pollMapper.updateById(poll);
        PollVO vo = toVO(poll);
        vo.setOptions(findOptions(id));
        return vo;
    }

    @Transactional
    public void delete(String id) {
        Poll poll = requireExist(id);
        if (!"DRAFT".equals(poll.getStatus())) {
            throw new BusinessException(ErrorCode.POLL_STATUS_ILLEGAL);
        }
        pollMapper.deleteById(poll.getId());
    }

    @Transactional
    public void vote(String id, VoteDTO dto) {
        Poll poll = requireExist(id);
        if (!"PUBLISHED".equals(poll.getStatus())) {
            throw new BusinessException(ErrorCode.POLL_STATUS_ILLEGAL);
        }
        if (poll.getDeadline() != null && LocalDateTime.now().isAfter(poll.getDeadline())) {
            throw new BusinessException(ErrorCode.POLL_EXPIRED);
        }

        String userId = StpUtil.getLoginIdAsString();
        boolean alreadyVoted = pollVoteMapper.exists(
                new LambdaQueryWrapper<PollVote>()
                        .eq(PollVote::getPollId, id)
                        .eq(PollVote::getUserId, userId));
        if (alreadyVoted) {
            throw new BusinessException(ErrorCode.POLL_ALREADY_VOTED);
        }

        List<String> optionIds = dto.getOptionIds();
        if ("SINGLE_CHOICE".equals(poll.getType()) && optionIds.size() > 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }

        List<PollOption> validOptions = pollOptionMapper.selectList(
                new LambdaQueryWrapper<PollOption>().eq(PollOption::getPollId, id));
        Map<String, Boolean> validMap = validOptions.stream()
                .collect(Collectors.toMap(PollOption::getId, o -> true));

        for (String optionId : optionIds) {
            if (!validMap.containsKey(optionId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST);
            }
            PollVote pv = new PollVote();
            pv.setPollId(id);
            pv.setOptionId(optionId);
            pv.setUserId(userId);
            pollVoteMapper.insert(pv);
        }
    }

    public PollResultVO result(String id) {
        Poll poll = requireExist(id);
        List<PollOption> options = pollOptionMapper.selectList(
                new LambdaQueryWrapper<PollOption>()
                        .eq(PollOption::getPollId, id)
                        .orderByAsc(PollOption::getSortOrder));
        List<PollVote> votes = pollVoteMapper.selectList(
                new LambdaQueryWrapper<PollVote>().eq(PollVote::getPollId, id));

        Map<String, Long> countByOption = votes.stream()
                .collect(Collectors.groupingBy(PollVote::getOptionId, Collectors.counting()));

        long totalUniqueVoters = votes.stream()
                .map(PollVote::getUserId)
                .distinct()
                .count();

        PollResultVO result = new PollResultVO();
        result.setPollId(id);
        result.setTitle(poll.getTitle());
        result.setType(poll.getType());
        result.setStatus(poll.getStatus());
        result.setTotalVotes((int) totalUniqueVoters);

        List<PollResultVO.OptionResultVO> optionResults = options.stream().map(opt -> {
            PollResultVO.OptionResultVO or = new PollResultVO.OptionResultVO();
            or.setOptionId(opt.getId());
            or.setContent(opt.getContent());
            or.setSortOrder(opt.getSortOrder());
            long cnt = countByOption.getOrDefault(opt.getId(), 0L);
            or.setVoteCount((int) cnt);
            or.setPercentage(totalUniqueVoters == 0 ? 0.0
                    : Math.round(cnt * 10000.0 / totalUniqueVoters) / 100.0);
            return or;
        }).collect(Collectors.toList());
        result.setOptions(optionResults);

        return result;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Poll requireExist(String id) {
        Poll poll = pollMapper.selectById(id);
        if (poll == null) throw new BusinessException(ErrorCode.POLL_NOT_FOUND);
        return poll;
    }

    private void applyDTO(Poll poll, PollDTO dto) {
        if (dto.getTitle() != null) poll.setTitle(dto.getTitle());
        if (dto.getDescription() != null) poll.setDescription(dto.getDescription());
        if (dto.getType() != null) poll.setType(dto.getType());
        if (dto.getDeadline() != null) poll.setDeadline(dto.getDeadline());
    }

    private List<PollOptionVO> findOptions(String pollId) {
        return pollOptionMapper.selectList(
                new LambdaQueryWrapper<PollOption>()
                        .eq(PollOption::getPollId, pollId)
                        .orderByAsc(PollOption::getSortOrder))
                .stream().map(this::toOptionVO).collect(Collectors.toList());
    }

    private PollVO toVO(Poll p) {
        PollVO vo = new PollVO();
        vo.setId(p.getId());
        vo.setTitle(p.getTitle());
        vo.setDescription(p.getDescription());
        vo.setType(p.getType());
        vo.setStatus(p.getStatus());
        vo.setAuthorId(p.getAuthorId());
        vo.setDeadline(p.getDeadline());
        vo.setCreatedAt(p.getCreatedAt());
        vo.setUpdatedAt(p.getUpdatedAt());
        return vo;
    }

    private PollOptionVO toOptionVO(PollOption o) {
        PollOptionVO vo = new PollOptionVO();
        vo.setId(o.getId());
        vo.setPollId(o.getPollId());
        vo.setContent(o.getContent());
        vo.setSortOrder(o.getSortOrder());
        vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }
}
