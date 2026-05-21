package com.property;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.entity.Role;
import com.property.entity.User;
import com.property.mapper.RoleMapper;
import com.property.mapper.UserMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * B1 集成测试：使用 Testcontainers 起 MySQL 容器，执行 Flyway V1+V2，
 * 验证实体映射与种子数据完整性。
 * Redis / RabbitMQ 用 MockBean 替代，不依赖 Docker Compose。
 *
 * 注意：本测试需要 Docker 环境，若本机未启动 Docker Desktop 则自动跳过。
 */
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "app.websocket.enabled=false",
        "management.health.redis.enabled=false",
        "management.health.rabbit.enabled=false"
})
class InfraIntegrationTest {

    static MySQLContainer<?> MYSQL;

    @BeforeAll
    static void startContainerIfDockerAvailable() {
        boolean dockerAvailable;
        try {
            dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            dockerAvailable = false;
        }
        assumeTrue(dockerAvailable, "Docker 不可用，跳过集成测试");

        MYSQL = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("property_db")
                .withUsername("root")
                .withPassword("root")
                .withCommand("--character-set-server=utf8mb4",
                             "--collation-server=utf8mb4_unicode_ci",
                             "--default-time-zone=+08:00");
        MYSQL.start();
    }

    @AfterAll
    static void stopContainer() {
        if (MYSQL != null && MYSQL.isRunning()) {
            MYSQL.stop();
        }
    }

    /** 用 Mock 代替真实 Redis，避免测试依赖 Docker Redis */
    @MockBean
    RedisConnectionFactory redisConnectionFactory;

    /** 用 Mock 代替真实 RabbitMQ */
    @MockBean
    ConnectionFactory rabbitConnectionFactory;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",      () -> MYSQL != null ? MYSQL.getJdbcUrl() : "");
        registry.add("spring.datasource.username",  () -> MYSQL != null ? MYSQL.getUsername() : "");
        registry.add("spring.datasource.password",  () -> MYSQL != null ? MYSQL.getPassword() : "");
        // 改用 HikariCP，避免 Druid 在 Testcontainers 连接串上的解析问题
        registry.add("spring.datasource.type",
                () -> "com.zaxxer.hikari.HikariDataSource");
    }

    @Autowired UserMapper userMapper;
    @Autowired RoleMapper roleMapper;

    @Test
    void seedUsersLoaded() {
        Long count = userMapper.selectCount(null);
        assertEquals(5L, count, "V2 seed 应有 5 个用户");
    }

    @Test
    void adminUserExists() {
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        assertNotNull(admin, "admin 用户必须存在");
        assertEquals("ACTIVE", admin.getStatus());
    }

    @Test
    void builtInRolesLoaded() {
        List<Role> roles = roleMapper.selectList(null);
        assertEquals(6, roles.size(), "应有 6 个内置角色");
        assertTrue(roles.stream().anyMatch(r -> "SuperAdmin".equals(r.getCode())));
    }
}
