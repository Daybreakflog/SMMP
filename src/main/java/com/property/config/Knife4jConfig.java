package com.property.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智能物业管理系统 API")
                        .version("0.1.0")
                        .description("前后端联调文档，遵循 OpenAPI 3.0 契约")
                        .contact(new Contact().name("物业技术团队")))
                .addSecurityItem(new SecurityRequirement().addList("satoken"))
                .components(new Components()
                        .addSecuritySchemes("satoken",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("satoken")
                                        .description("Sa-Token 访问令牌，登录后从响应的 token.accessToken 获取")));
    }
}
