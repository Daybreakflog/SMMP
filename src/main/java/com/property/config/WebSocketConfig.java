package com.property.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {

    /**
     * ServerEndpointExporter 需要真实 Servlet 容器（嵌入式 Tomcat）提供
     * jakarta.websocket.server.ServerContainer。
     * 在 @SpringBootTest(webEnvironment=MOCK) 测试环境中不可用，
     * 通过 ConditionalOnProperty 使其可被测试跳过。
     */
    @Bean
    @ConditionalOnProperty(name = "app.websocket.enabled", matchIfMissing = true)
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
