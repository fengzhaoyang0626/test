package com.example.newproiect;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class ApplicationSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeHealthAndSwagger() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("简易管理系统 API"));

        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }

    @Test
    void shouldCreateAndReadUser() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "smoke_user",
                                  "password": "password123",
                                  "displayName": "冒烟测试用户",
                                  "email": "smoke@example.com",
                                  "phone": "13800000000",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username").value("smoke_user"))
                .andReturn();

        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10")
                        .param("keyword", "smoke_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("smoke_user"));

        Number idValue = JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.data.id"
        );
        assertThat(idValue).isNotNull();
        long userId = idValue.longValue();

        mockMvc.perform(post("/api/users/batch-query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ids": [%d, %d, 999999]
                                }
                                """.formatted(userId, userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.results.length()").value(2))
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.results[0].user.username").value("smoke_user"))
                .andExpect(jsonPath("$.data.results[1].errorCode").value("USER_NOT_FOUND"));
    }

    @Test
    void shouldUpdateAndLogicallyDeleteUser() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "crud_user",
                                  "password": "password123",
                                  "displayName": "待修改用户",
                                  "status": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Number idValue = JsonPath.read(
                createResult.getResponse().getContentAsString(),
                "$.data.id"
        );
        assertThat(idValue).isNotNull();
        long userId = idValue.longValue();

        mockMvc.perform(put("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "已修改用户",
                                  "email": "updated@example.com",
                                  "phone": "13900000000",
                                  "status": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("已修改用户"))
                .andExpect(jsonPath("$.data.status").value(0));

        mockMvc.perform(get("/api/users/list")
                        .param("name", "updated@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(userId));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("已修改用户"))
                .andExpect(jsonPath("$.data.status").value(0));

        mockMvc.perform(delete("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }
}
