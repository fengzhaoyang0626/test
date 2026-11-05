package com.example.newproiect.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI managementOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("简易管理系统 API")
                        .version("0.0.1")
                        .description("用户管理第一版接口。打开 Swagger UI 后可以直接调用接口。"));
    }
}
