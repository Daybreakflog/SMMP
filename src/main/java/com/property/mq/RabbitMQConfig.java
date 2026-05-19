package com.property.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.property.constant.RabbitMQConstants.*;

@Configuration
public class RabbitMQConfig {

    @Bean TopicExchange billExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_BILL).durable(true).build();
    }

    @Bean TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_NOTIFICATION).durable(true).build();
    }

    @Bean TopicExchange workorderExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_WORKORDER).durable(true).build();
    }

    @Bean DirectExchange reportExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_REPORT).durable(true).build();
    }

    @Bean DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE_DLX).durable(true).build();
    }

    private Map<String, Object> dlxArgs() {
        return Map.of(
                "x-dead-letter-exchange", EXCHANGE_DLX,
                "x-dead-letter-routing-key", RK_DLQ
        );
    }

    @Bean Queue queueBillGenerate() {
        return QueueBuilder.durable(QUEUE_BILL_GENERATE).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueBillOverdue() {
        return QueueBuilder.durable(QUEUE_BILL_OVERDUE).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueNotificationInApp() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_IN_APP).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueNotificationSms() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_SMS).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueNotificationPush() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_PUSH).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueNotificationEmail() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION_EMAIL).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueWorkorderAssigned() {
        return QueueBuilder.durable(QUEUE_WORKORDER_ASSIGNED).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueWorkorderCompleted() {
        return QueueBuilder.durable(QUEUE_WORKORDER_COMPLETED).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueReportExport() {
        return QueueBuilder.durable(QUEUE_REPORT_EXPORT).withArguments(dlxArgs()).build();
    }

    @Bean Queue queueDlq() {
        return QueueBuilder.durable(QUEUE_DLQ).build();
    }

    @Bean Binding bindBillGenerate(Queue queueBillGenerate, TopicExchange billExchange) {
        return BindingBuilder.bind(queueBillGenerate).to(billExchange).with(RK_BILL_GENERATE);
    }

    @Bean Binding bindBillOverdue(Queue queueBillOverdue, TopicExchange billExchange) {
        return BindingBuilder.bind(queueBillOverdue).to(billExchange).with(RK_BILL_OVERDUE);
    }

    @Bean Binding bindNotificationInApp(Queue queueNotificationInApp, TopicExchange notificationExchange) {
        return BindingBuilder.bind(queueNotificationInApp).to(notificationExchange).with(RK_NOTIFICATION_IN_APP);
    }

    @Bean Binding bindNotificationSms(Queue queueNotificationSms, TopicExchange notificationExchange) {
        return BindingBuilder.bind(queueNotificationSms).to(notificationExchange).with(RK_NOTIFICATION_SMS);
    }

    @Bean Binding bindNotificationPush(Queue queueNotificationPush, TopicExchange notificationExchange) {
        return BindingBuilder.bind(queueNotificationPush).to(notificationExchange).with(RK_NOTIFICATION_PUSH);
    }

    @Bean Binding bindNotificationEmail(Queue queueNotificationEmail, TopicExchange notificationExchange) {
        return BindingBuilder.bind(queueNotificationEmail).to(notificationExchange).with(RK_NOTIFICATION_EMAIL);
    }

    @Bean Binding bindWorkorderAssigned(Queue queueWorkorderAssigned, TopicExchange workorderExchange) {
        return BindingBuilder.bind(queueWorkorderAssigned).to(workorderExchange).with(RK_WORKORDER_ASSIGNED);
    }

    @Bean Binding bindWorkorderCompleted(Queue queueWorkorderCompleted, TopicExchange workorderExchange) {
        return BindingBuilder.bind(queueWorkorderCompleted).to(workorderExchange).with(RK_WORKORDER_COMPLETED);
    }

    @Bean Binding bindReportExport(Queue queueReportExport, DirectExchange reportExchange) {
        return BindingBuilder.bind(queueReportExport).to(reportExchange).with(RK_REPORT_EXPORT);
    }

    @Bean Binding bindDlq(Queue queueDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(queueDlq).to(dlxExchange).with(RK_DLQ);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(converter);
        template.setMandatory(true);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory factory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(factory);
        f.setMessageConverter(converter);
        f.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        f.setPrefetchCount(10);
        return f;
    }
}
