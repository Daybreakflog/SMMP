package com.property.mq;

import com.property.dto.request.NotificationMessage;

import com.property.entity.Notification;
import com.property.mapper.NotificationMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.property.constant.RabbitMQConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationMapper notificationMapper;

    @RabbitListener(queues = QUEUE_NOTIFICATION_IN_APP)
    public void handleInApp(NotificationMessage msg,
                            Channel ch,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        persist(msg, ch, tag);
    }

    @RabbitListener(queues = QUEUE_NOTIFICATION_SMS)
    public void handleSms(NotificationMessage msg,
                          Channel ch,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        persist(msg, ch, tag);
        // SMS delivery stub — integrate third-party gateway here
    }

    @RabbitListener(queues = QUEUE_NOTIFICATION_PUSH)
    public void handlePush(NotificationMessage msg,
                           Channel ch,
                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        persist(msg, ch, tag);
        // Push delivery stub — integrate FCM/APNs here
    }

    private void persist(NotificationMessage msg, Channel ch, long tag) {
        try {
            Notification n = new Notification();
            n.setUserId(msg.getUserId());
            n.setType(msg.getType());
            n.setChannel(msg.getChannel());
            n.setTitle(msg.getTitle());
            n.setContent(msg.getContent());
            n.setIsRead(false);
            n.setStatus("SENT");
            n.setTargetId(msg.getTargetId());
            n.setTargetType(msg.getTargetType());
            notificationMapper.insert(n);
            ch.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to persist notification for user={}: {}", msg.getUserId(), e.getMessage(), e);
            try {
                ch.basicNack(tag, false, false);
            } catch (IOException ex) {
                log.error("Failed to nack message tag={}", tag, ex);
            }
        }
    }
}
