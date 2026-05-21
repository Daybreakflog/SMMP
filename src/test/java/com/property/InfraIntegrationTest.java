package com.property;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.property.entity.Role;
import com.property.entity.User;
import com.property.mapper.RoleMapper;
import com.property.mapper.UserMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.MySQLContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * B1 集成测试：使用 Testcontainers 起 MySQL 容器，执行 Flyway 全量迁移，
 * 验证实体映射与种子数据完整性。
 * 排除 Redis / RabbitMQ / Sa-Token-Redis 全部自动配置，仅测试 MySQL 数据层。
 *
 * 注意：本测试需要 Docker 环境，若本机未启动 Docker Desktop 则自动跳过。
 */
@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "app.websocket.enabled=false",
        // 禁用 Redis / RabbitMQ 健康检查
        "management.health.redis.enabled=false",
        "management.health.rabbit.enabled=false",
        // 排除所有 Redis（含 Reactive）、RabbitMQ、Sa-Token-Redis 自动配置
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration," +
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                "cn.dev33.satoken.dao.SaTokenDaoRedisJackson"
})
class InfraIntegrationTest {

    static MySQLContainer<?> MYSQL;

    // ---- Mock 基础设施 bean，满足应用层 @Configuration / @Service 依赖 ----
    @MockBean RedisConnectionFactory redisConnectionFactory;
    @MockBean StringRedisTemplate stringRedisTemplate;
    @MockBean org.springframework.amqp.rabbit.connection.ConnectionFactory amqpConnectionFactory;

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
