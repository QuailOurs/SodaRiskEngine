package com.soda.risk.engine.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc Swagger配置 - API文档自动生成
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI riskEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("业务安全风控引擎 API")
                        .description("风险引擎核心服务API文档，包含策略引擎、风险决策、处置引擎等接口")
                        .version("3.0.0")
                        .contact(new Contact()
                                .name("安全风控团队")
                        .email("security@example.invalid"))
                        .license(new License()
                                .name("Internal Use Only")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("开发环境"),
                        new Server().url("https://risk-engine.example.com").description("生产环境")
                ));
    }
}
