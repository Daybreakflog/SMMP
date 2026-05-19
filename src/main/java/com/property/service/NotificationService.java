package com.property.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.property.common.api.PageResult;
import com.property.entity.Notification;
import com.property.dto.request.NotificationQueryDTO;
import com.property.dto.request.SendNotificationDTO;
import com.property.dto.response.NotificationVO;

public interface NotificationService {

    void send(SendNotificationDTO dto);

    PageResult<NotificationVO> my(NotificationQueryDTO query);

    NotificationVO getById(String id);

    void markRead(String id);

    void markAllRead();

    long unreadCount();

    void delete(String id);
}
