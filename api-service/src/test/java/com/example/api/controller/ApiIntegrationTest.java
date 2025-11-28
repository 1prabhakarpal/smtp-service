package com.example.api.controller;

import com.example.api.dto.EmailRequest;
import com.example.api.dto.FolderRequest;
import com.example.api.dto.LoginRequest;
import com.example.common.entity.Settings;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;

    @Test
    @Order(1)
    public void testRegister() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "testuser");
        payload.put("password", "password123");

        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(2)
    public void testLogin() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Map<String, Object> map = objectMapper.readValue(response, Map.class);
        jwtToken = (String) map.get("token");
    }

    @Test
    @Order(3)
    public void testGetMe() throws Exception {
        mockMvc.perform(get("/api/me")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @Order(4)
    public void testCreateFolder() throws Exception {
        FolderRequest request = new FolderRequest();
        request.setName("Work");

        mockMvc.perform(post("/api/folders")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    public void testGetFolders() throws Exception {
        mockMvc.perform(get("/api/folders")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Work"));
    }

    @Test
    @Order(6)
    public void testSendEmail() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setTo("recipient@example.com");
        request.setSubject("Test Subject");
        request.setBody("Test Body");

        mockMvc.perform(post("/api/emails")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    public void testGetEmails() throws Exception {
        mockMvc.perform(get("/api/emails")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(8)
    public void testUpdateSettings() throws Exception {
        Settings settings = new Settings();
        settings.setTheme("dark");
        settings.setNotificationsEnabled(true);

        mockMvc.perform(patch("/api/settings")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.theme").value("dark"));
    }

    @Test
    @Order(9)
    public void testLogout() throws Exception {
        mockMvc.perform(post("/api/logout")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }
}
