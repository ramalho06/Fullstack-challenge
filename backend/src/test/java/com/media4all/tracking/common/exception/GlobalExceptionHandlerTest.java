package com.media4all.tracking.common.exception;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.external.ExternalApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void returnsNotFoundForResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test-errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("Agent not found"))
                .andExpect(jsonPath("$.error.details").value("Agent id=missing-agent"));
    }

    @Test
    void returnsUnprocessableEntityForValidationError() throws Exception {
        mockMvc.perform(post("/test-errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Validation failed"))
                .andExpect(jsonPath("$.error.details", containsString("name:")));
    }

    @Test
    void returnsBadRequestForIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/test-errors/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("Invalid request"));
    }

    @Test
    void returnsBadRequestForInvalidSortProperty() throws Exception {
        mockMvc.perform(get("/test-errors/invalid-sort"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("Invalid sort property"))
                .andExpect(jsonPath("$.error.details").value("string"));
    }

    @Test
    void returnsConflictForConflictException() throws Exception {
        mockMvc.perform(get("/test-errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"))
                .andExpect(jsonPath("$.error.message").value("Resource conflict"));
    }

    @Test
    void returnsInternalServerErrorForUnexpectedException() throws Exception {
        mockMvc.perform(get("/test-errors/internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Unexpected internal error"));
    }

    @Test
    void returnsTooManyRequestsForExternalApiRateLimit() throws Exception {
        mockMvc.perform(get("/test-errors/external-rate-limit"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("EXTERNAL_API_ERROR"))
                .andExpect(jsonPath("$.error.message").value("Rate limited"))
                .andExpect(jsonPath("$.error.details").value("Too many requests"));
    }

    @RestController
    @RequestMapping("/test-errors")
    static class TestController {

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Agent", "missing-agent");
        }

        @PostMapping("/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/bad-request")
        void badRequest() {
            throw new IllegalArgumentException("Invalid request");
        }

        @GetMapping("/invalid-sort")
        void invalidSort() {
            throw new PropertyReferenceException("string", TypeInformation.of(Agent.class), List.of());
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new ConflictException("Resource conflict");
        }

        @GetMapping("/internal")
        void internal() {
            throw new RuntimeException("Database password leaked in stacktrace? no");
        }

        @GetMapping("/external-rate-limit")
        void externalRateLimit() {
            throw new ExternalApiException("Rate limited", 429, "Too many requests");
        }
    }

    record TestRequest(
            @NotBlank
            String name
    ) {
    }
}
