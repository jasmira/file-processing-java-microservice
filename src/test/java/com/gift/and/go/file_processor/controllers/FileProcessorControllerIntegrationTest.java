package com.gift.and.go.file_processor.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class FileProcessorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("testProcessFileEndpoint_withCorrectFile")
    void testProcessFileEndpoint_withCorrectFile() throws Exception {
        // Prepare test file content
        String fileContent = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n" +
                "\\n\n" +
                "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5";

        // Create a mock MultipartFile object with the test file content
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", fileContent.getBytes());
        String ipAddress = "103.24.49.0";
        boolean skipValidation = false;

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/process-file")
                        .file(file)
                        .param("ipAddress", ipAddress)
                        .param("skipValidation", String.valueOf(skipValidation)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].Name").value("John Smith"))
                .andExpect(jsonPath("$[0].Transport").value("Rides A Bike"))
                .andExpect(jsonPath("$[0]['Top Speed']").value(12.1))
                .andExpect(jsonPath("$[1].Name").value("Mike Smith"))
                .andExpect(jsonPath("$[1].Transport").value("Drives an SUV"))
                .andExpect(jsonPath("$[1]['Top Speed']").value(95.5));
    }

    @Test
    @DisplayName("testProcessFileEndpoint_withEmptyFile")
    void testProcessFileEndpoint_withEmptyFile() throws Exception {
        // Create a mock MultipartFile object with the test file content as empty.
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "".getBytes());
        String ipAddress = "103.24.49.0";
        boolean skipValidation = false;

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/process-file")
                        .file(file)
                        .param("ipAddress", ipAddress)
                        .param("skipValidation", String.valueOf(skipValidation)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("Failed to process the file: File is empty."));
    }

    @Test
    @DisplayName("testValidateIpEndpoint_WithValidIP")
    void testValidateIpEndpoint_WithValidIP() throws Exception {
        String ipAddress = "103.24.49.0";

        mockMvc.perform(get("/api/validate-ip")
                        .param("ipAddress", ipAddress))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.blockedCountry").value(false))
                .andExpect(jsonPath("$.blockedISP").value(false))
                .andExpect(jsonPath("$.country").value("Indonesia"))
                .andExpect(jsonPath("$.countryCode").value("ID"))
                .andExpect(jsonPath("$.isp").value("Politeknik Perkapalan Negeri Surabaya"))
                .andExpect(jsonPath("$.reason").value("IP address is whitelisted."))
                .andExpect(jsonPath("$.httpStatusCode").value(200));
    }

    @Test
    @DisplayName("testValidateIpEndpoint_WithBlockedCountryIP")
    void testValidateIpEndpoint_WithBlockedCountryIP() throws Exception {
        String ipAddress = "8.8.8.8";

        mockMvc.perform(get("/api/validate-ip")
                        .param("ipAddress", ipAddress))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.blockedCountry").value(true))
                .andExpect(jsonPath("$.blockedISP").value(false))
                .andExpect(jsonPath("$.country").value("United States"))
                .andExpect(jsonPath("$.countryCode").value("US"))
                .andExpect(jsonPath("$.isp").value("Google LLC"))
                .andExpect(jsonPath("$.reason").value("Access from the country: United States is not allowed."))
                .andExpect(jsonPath("$.httpStatusCode").value(403));
    }

    @Test
    @DisplayName("testValidateIpEndpoint_WithBlockedIspIP")
    void testValidateIpEndpoint_WithBlockedIspIP() throws Exception {
        String ipAddress = "52.66.193.64";

        mockMvc.perform(get("/api/validate-ip")
                        .param("ipAddress", ipAddress))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.blockedCountry").value(false))
                .andExpect(jsonPath("$.blockedISP").value(true))
                .andExpect(jsonPath("$.country").value("India"))
                .andExpect(jsonPath("$.countryCode").value("IN"))
                .andExpect(jsonPath("$.isp").value("Amazon Technologies Inc."))
                .andExpect(jsonPath("$.reason").value("Access from the ISP: Amazon Technologies Inc. is not allowed."))
                .andExpect(jsonPath("$.httpStatusCode").value(403));
    }

    @Test
    @DisplayName("testValidateIpDetailsEndpoint_WithValidIP")
    void testValidateIpDetailsEndpoint_WithValidIP() throws Exception {
        String ipAddress = "103.24.49.0";

        mockMvc.perform(get("/api/validate-ip/details")
                        .param("ipAddress", ipAddress))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("IP address is whitelisted."));
    }

    @Test
    @DisplayName("testValidateIpDetailsEndpoint_WithBlockedCountryIP")
    void testValidateIpDetailsEndpoint_WithBlockedCountryIP() throws Exception {
        String ipAddress = "8.8.8.8";

        mockMvc.perform(get("/api/validate-ip/details")
                        .param("ipAddress", ipAddress))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Access from the country: United States is not allowed."));
    }

    @Test
    @DisplayName("testValidateIpDetailsEndpoint_WithBlockedIspIP")
    void testValidateIpDetailsEndpoint_WithBlockedIspIP() throws Exception {
        String ipAddress = "52.66.193.64";

        mockMvc.perform(get("/api/validate-ip/details")
                        .param("ipAddress", ipAddress))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string("Access from the ISP: Amazon Technologies Inc. is not allowed."));
    }
}
