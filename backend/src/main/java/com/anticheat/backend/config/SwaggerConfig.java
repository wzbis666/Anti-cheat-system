package com.anticheat.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("AntiCheat 反作弊系统 API")
                .version("1.0.0")
                .description("AntiCheat反作弊管理系统后端API接口文档\n\n" +
                    "## 功能模块\n" +
                    "- 用户认证与管理\n" +
                    "- 玩家管理\n" +
                    "- 作弊记录管理\n" +
                    "- 封禁管理\n" +
                    "- 白名单管理\n" +
                    "- 举报管理\n" +
                    "- 系统统计\n\n" +
                    "## 认证方式\n" +
                    "使用JWT Bearer Token认证，在请求头中添加: `Authorization: Bearer <token>`")
                .contact(new Contact()
                    .name("AntiCheat Team")
                    .email("admin@localhost"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer"))
            .components(new Components()
                .addSecuritySchemes("Bearer", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT认证令牌")));
    }
}
