package com.property;

import org.junit.jupiter.api.Test;

/**
 * B0 占位测试，不启动 Spring Context（需要 docker-compose 服务就绪才能集成测试）。
 * 集成测试在 B1+ 里程碑中用 Testcontainers 实现。
 */
class PropertyApplicationTests {

    @Test
    void placeholder() {
        // no-op：保证 mvn clean install 在无 docker 服务时也能通过
    }
}
