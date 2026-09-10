package com.autopay.parking.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminWebTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void loginValidoDevuelveToken() throws Exception {
        mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.fullName").isNotEmpty());
    }

    @Test
    void loginInvalidoRechazado() throws Exception {
        mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"mal\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void panelRequiereSesion() throws Exception {
        mvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboardConSesionDevuelveDatos() throws Exception {
        String token = loginToken();
        mvc.perform(get("/api/admin/dashboard")
                        .header(AdminAuthFilter.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeTickets").exists())
                .andExpect(jsonPath("$.totalCollected").isNotEmpty());
    }

    @Test
    void reportesConSesionDevuelveFilas() throws Exception {
        String token = loginToken();
        mvc.perform(get("/api/admin/reports")
                        .header(AdminAuthFilter.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows").isArray())
                .andExpect(jsonPath("$.total").isNotEmpty());
    }

    @Test
    void panelWebSeSirveSinLogin() throws Exception {
        mvc.perform(get("/admin/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void ticketsConSesionDevuelveListado() throws Exception {
        String token = loginToken();
        mvc.perform(get("/api/admin/tickets")
                        .header(AdminAuthFilter.TOKEN_HEADER, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").isNotEmpty())
                .andExpect(jsonPath("$[0].status").isNotEmpty());
    }

    private String loginToken() throws Exception {
        MvcResult result = mvc.perform(post("/api/admin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}