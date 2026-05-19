package com.property.service.impl;

import com.property.service.NotificationService;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.exception.BusinessException;
import com.property.exception.ErrorCode;
import com.property.entity.MessageTemplate;
import com.property.entity.Notification;
import com.property.entity.User;
import com.property.entity.UserRole;
import com.property.mapper.MessageTemplateMapper;
import com.property.mapper.NotificationMapper;
import com.property.mapper.UserMapper;
import com.property.mapper.UserRoleMapper;
import com.property.dto.request.NotificationQueryDTO;
import com.property.dto.request.SendNotificationDTO;
import com.property.dto.request.NotificationMessage;
import com.property.dto.response.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.property.constant.RabbitMQConstants.EXCHANGE_NOTIFICATION;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final MessageTemplateMapper messageTemplateMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RabbitTemplate rabbitTemplate;

    public void send(SendNotificationDTO dto) {
        String title = dto.getTitle();
        String content = dto.getContent();

        if (StrUtil.isNotBlank(dto.getTemplateCode())) {
            MessageTemplate tpl = messageTemplateMapper.selectOne(
                    new LambdaQueryWrapper<MessageTemplate>()
                            .eq(MessageTemplate::getCode, dto.getTemplateCode()));
            if (tpl == null) throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
            Map<String, Object> params = dto.getParams() != null ? dto.getParams() : Map.of();
            title = StrUtil.format(tpl.getTitle(), params);
            content = StrUtil.format(tpl.getContent(), params);
        }

        List<String> userIds = resolveTargetUsers(dto.getTargetType(), dto.getTargetId());

        String channel = StrUtil.blankToDefault(dto.getChannel(), "IN_APP");
        String routingKey = "notification." + channel.toLowerCase();

        for (String userId : userIds) {
            NotificationMessage msg = new NotificationMessage();
            msg.setUserId(userId);
            msg.setType(dto.getType());
            msg.setChannel(channel);
            msg.setTitle(title);
            msg.setContent(content);
            msg.setTargetId(dto.getTargetId());
            msg.setTargetType(dto.getTargetType());
            rabbitTemplate.convertAndSend(EXCHANGE_NOTIFICATION, routingKey, msg);
        }
    }

    public PageResult<NotificationVO> my(NotificationQueryDTO query) {
        String userId = StpUtil.getLoginIdAsString();
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(query.getRead() != null, Notification::getIsRead, query.getRead())
                .eq(StrUtil.isNotBlank(query.getType()), Notification::getType, query.getType())
                .orderByDesc(Notification::getCreatedAt);
        return PageResult.of(notificationMapper.selectPage(
                new Page<>(query.getPage(), query.getPageSize()), wrapper
        ).convert(this::toVO));
    }

    @Transactional
    public NotificationVO getById(String id) {
        Notification n = requireOwn(id);
        if (!Boolean.TRUE.equals(n.getIsRead())) {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(n);
        }
        return toVO(n);
    }

    @Transactional
    public void markRead(String id) {
        Notification n = requireOwn(id);
        if (!Boolean.TRUE.equals(n.getIsRead())) {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationMapper.updateById(n);
        }
    }

    @Transactional
    public void markAllRead() {
        String userId = StpUtil.getLoginIdAsString();
        notificationMapper.update(null, new LambdaUpdateWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false)
                .set(Notification::getIsRead, true)
                .set(Notification::getReadAt, LocalDateTime.now()));
    }

    public long unreadCount() {
        String userId = StpUtil.getLoginIdAsString();
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, false));
    }

    @Transactional
    public void delete(String id) {
        requireOwn(id);
        notificationMapper.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Notification requireOwn(String id) {
        Notification n = notificationMapper.selectById(id);
        if (n == null) throw new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND);
        if (!n.getUserId().equals(StpUtil.getLoginIdAsString())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return n;
    }

    private List<String> resolveTargetUsers(String targetType, String targetId) {
        return switch (targetType) {
            case "USER" -> List.of(targetId);
            case "ROLE" -> userRoleMapper.selectList(
                            new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleCode, targetId))
                    .stream().map(UserRole::getUserId).toList();
            case "ALL" -> userMapper.selectList(
                            new LambdaQueryWrapper<User>().select(User::getId))
                    .stream().map(User::getId).toList();
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
        };
    }

    private NotificationVO toVO(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setUserId(n.getUserId());
        vo.setType(n.getType());
        vo.setChannel(n.getChannel());
        vo.setStatus(n.getStatus());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setIsRead(n.getIsRead());
        vo.setReadAt(n.getReadAt());
        vo.setTargetId(n.getTargetId());
        vo.setTargetType(n.getTargetType());
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
