package com.nexarats.academy.common.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HealthCheckControllerTests {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new HealthCheckController())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void healthEndpointReturnsOkAndPreservesCorrelationId() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, "test-trace-uuid-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, "test-trace-uuid-123"))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("nexarats-academy-core"))
                .andExpect(jsonPath("$.version").value("0.0.1-SNAPSHOT"));
    }

    @Test
    void healthEndpointGeneratesCorrelationIdIfAbsent() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
