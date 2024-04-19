package com.gift.and.go.file_processor.services;

import com.gift.and.go.file_processor.dtos.ValidationResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IPValidatorServiceTest {
    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void setUp() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @AfterAll
    public static void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("test_validateIPAddress_success")
    public void testValidateIPAddress_Success() {
        // Set up WireMock stub for a successful response
        String successResponse = "{\"status\":\"success\",\"country\":\"Canada\",\"countryCode\":\"CA\",\"isp\":\"Le Groupe Videotron Ltee\",\"org\":\"Videotron Ltee\",\"query\":\"24.48.0.1\"}";
        stubFor(get(urlEqualTo("/json/24.48.0.1?fields=status,country,countryCode,isp,org,query"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResponse)));

        // Create an instance of IPValidatorService with mocked RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // API Hit: "http://localhost:" + wireMockServer.port() + "/json/24.48.0.1?fields=status,country,countryCode,isp,org,query";
        IPValidatorService ipValidatorService = new IPValidatorService(restTemplate);

        // Call the validateIPAddress method
        ValidationResponse response = ipValidatorService.validateIPAddress("24.48.0.1");

        // Verify the response
        assertTrue(response.isValid());
        assertFalse(response.isBlockedCountry());
        assertFalse(response.isBlockedISP());
        assertEquals("Canada", response.getCountry());
        assertEquals("CA", response.getCountryCode());
        assertEquals("Le Groupe Videotron Ltee", response.getIsp());
        assertEquals("IP address is whitelisted.", response.getReason());
        assertEquals(HttpStatus.OK.value(), response.getHttpStatusCode());
    }

    @Test
    @DisplayName("test_validateIPAddress_forBlockedCountry")
    public void testValidateIPAddress_blockedCountry() {
        // Set up WireMock stub for a successful response
        String successResponse = "{\"status\":\"success\",\"country\":\"United States\",\"countryCode\":\"US\",\"isp\":\"Google LLC\",\"org\":\"Google Public DNS\",\"query\":\"8.8.8.8\"}";
        stubFor(get(urlEqualTo("/json/8.8.8.8?fields=status,country,countryCode,isp,org,query"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.FORBIDDEN.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResponse)));

        // Create an instance of IPValidatorService with mocked RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // API Hit: "http://localhost:" + wireMockServer.port() + "/json/8.8.8.8?fields=status,country,countryCode,isp,org,query";
        IPValidatorService ipValidatorService = new IPValidatorService(restTemplate);

        // Call the validateIPAddress method
        ValidationResponse response = ipValidatorService.validateIPAddress("8.8.8.8");

        // Verify the response
        assertFalse(response.isValid());
        assertTrue(response.isBlockedCountry());
        assertFalse(response.isBlockedISP());
        assertEquals("United States", response.getCountry());
        assertEquals("US", response.getCountryCode());
        assertEquals("Google LLC", response.getIsp());
        assertEquals("Access from the country: United States is not allowed.", response.getReason());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getHttpStatusCode());
    }

    @Test
    @DisplayName("test_validateIPAddress_forBlockedISP")
    public void testValidateIPAddress_blockedISP() {
        // Set up WireMock stub for a successful response
        String successResponse = "{\"status\":\"success\",\"country\":\"India\",\"countryCode\":\"IN\",\"isp\":\"Amazon Technologies Inc.\",\"org\":\"AWS EC2 (ap-south-1)\",\"query\":\"52.66.193.64\"}";
        stubFor(get(urlEqualTo("/json/52.66.193.64?fields=status,country,countryCode,isp,org,query"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.FORBIDDEN.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResponse)));

        // Create an instance of IPValidatorService with mocked RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // API Hit: "http://localhost:" + wireMockServer.port() + "/json/52.66.193.64?fields=status,country,countryCode,isp,org,query";
        IPValidatorService ipValidatorService = new IPValidatorService(restTemplate);

        // Call the validateIPAddress method
        ValidationResponse response = ipValidatorService.validateIPAddress("52.66.193.64");

        // Verify the response
        assertFalse(response.isValid());
        assertFalse(response.isBlockedCountry());
        assertTrue(response.isBlockedISP());
        assertEquals("India", response.getCountry());
        assertEquals("IN", response.getCountryCode());
        assertEquals("Amazon Technologies Inc.", response.getIsp());
        assertEquals("Access from the ISP: Amazon Technologies Inc. is not allowed.", response.getReason());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getHttpStatusCode());
    }

    @Test
    @DisplayName("test_validateIPAddress_failure")
    public void testValidateIPAddress_Failure() {
        // Set up WireMock stub for a successful response
        String successResponse = "{\"status\":\"fail\",\"country\":\"null\",\"countryCode\":\"null\",\"isp\":\"null\",\"org\":\"null\",\"query\":\"127.0.0.1\"}";
        stubFor(get(urlEqualTo("/json/127.0.0.1?fields=status,country,countryCode,isp,org,query"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(successResponse)));

        // Create an instance of IPValidatorService with mocked RestTemplate
        RestTemplate restTemplate = new RestTemplate();
        // API Hit: "http://localhost:" + wireMockServer.port() + "/json/127.0.0.1?fields=status,country,countryCode,isp,org,query";
        IPValidatorService ipValidatorService = new IPValidatorService(restTemplate);

        // Call the validateIPAddress method
        ValidationResponse response = ipValidatorService.validateIPAddress("127.0.0.1");

        // Verify the response
        assertFalse(response.isValid());
        assertFalse(response.isBlockedCountry());
        assertFalse(response.isBlockedISP());
        assertNull(response.getCountry());
        assertEquals("INVALID", response.getCountryCode());
        assertEquals("INVALID", response.getIsp());
        assertEquals("Failed to validate IP address.", response.getReason());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getHttpStatusCode());
    }
}
