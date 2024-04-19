package com.gift.and.go.file_processor.controllers;

import com.gift.and.go.file_processor.dtos.ValidationResponse;
import com.gift.and.go.file_processor.services.FileProcessingService;
import com.gift.and.go.file_processor.services.IPValidatorService;
import com.gift.and.go.file_processor.services.RequestLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class FileProcessorControllerTest {

    @Mock
    private FileProcessingService fileProcessingService;

    @Mock
    private IPValidatorService ipValidatorService;

    @Mock
    private RequestLogService requestLogService;

    @InjectMocks
    private FileProcessorController fileProcessorController;

    @Test
    @DisplayName("testProcessFile_WithValidIPAndSkipValidationTrue_ShouldReturnOK")
    public void testProcessFile_WithValidIPAndSkipValidationTrue_ShouldReturnOK() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        String ipAddress = "103.24.49.0";

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, true);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("testProcessFile_WithValidIPAndSkipValidationFalse_ShouldReturnOK")
    public void testProcessFile_WithValidIPAndSkipValidationFalse_ShouldReturnOK() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        String ipAddress = "103.24.49.0";

        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(true, false, false, "Indonesia", "ID", "Politeknik Perkapalan Negeri Surabaya", "IP address is whitelisted.", HttpStatus.OK.value()));

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, false);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("testProcessFile_WithInvalidIP_ShouldReturnInternalServerError")
    public void testProcessFile_WithInvalidIP_ShouldReturnInternalServerError() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        String ipAddress = "127.0.0.1";

        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, false, false, null, null, null, "Failed to validate IP address.", HttpStatus.INTERNAL_SERVER_ERROR.value()));

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, false);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("testProcessFile_WithBlockedCountry_ShouldReturnForbidden")
    public void testProcessFile_WithBlockedCountry_ShouldReturnForbidden() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        String ipAddress = "8.8.8.8";

        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, true, false, "United States", "US", "Google LLC", "Access from the country: United States is not allowed.", HttpStatus.FORBIDDEN.value()));

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, false);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("testProcessFile_WithBlockedISP_ShouldReturnForbidden")
    public void testProcessFile_WithBlockedISP_ShouldReturnForbidden() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "Hello, World!".getBytes());
        String ipAddress = "51.104.9.100";

        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, false, true, "United Kingdom", "GB", "Microsoft Corporation", "Access from the ISP: Microsoft Corporation is not allowed.", HttpStatus.FORBIDDEN.value()));

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, false);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("testValidateIpAddress_WithValidIpAddress_ShouldReturnOK")
    void testValidateIpAddress_WithValidIpAddress_ShouldReturnOK() {
        // Arrange
        String ipAddress = "103.24.49.0";
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(true, false, false, "Indonesia", "ID", "Politeknik Perkapalan Negeri Surabaya", "IP address is whitelisted.", HttpStatus.OK.value()));

        // Act
        ResponseEntity<ValidationResponse> response = fileProcessorController.validateIpAddress(ipAddress);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        assertFalse(response.getBody().isBlockedCountry());
        assertFalse(response.getBody().isBlockedISP());
        assertEquals("Indonesia", response.getBody().getCountry());
        assertEquals("ID", response.getBody().getCountryCode());
        assertEquals("Politeknik Perkapalan Negeri Surabaya", response.getBody().getIsp());
        assertEquals("IP address is whitelisted.", response.getBody().getReason());
    }

    @Test
    @DisplayName("testValidateIpAddress_WithBlockedCountry_ShouldReturnForbidden")
    void testValidateIpAddress_WithBlockedCountry_ShouldReturnForbidden() {
        // Arrange
        String ipAddress = "8.8.8.8";
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, true, false, "United States", "US", "Google LLC", "Access from the country: United States is not allowed.", HttpStatus.FORBIDDEN.value()));

        // Act
        ResponseEntity<ValidationResponse> response = fileProcessorController.validateIpAddress(ipAddress);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertTrue(response.getBody().isBlockedCountry());
        assertFalse(response.getBody().isBlockedISP());
        assertEquals("United States", response.getBody().getCountry());
        assertEquals("US", response.getBody().getCountryCode());
        assertEquals("Google LLC", response.getBody().getIsp());
        assertEquals("Access from the country: United States is not allowed.", response.getBody().getReason());
    }

    @Test
    @DisplayName("testValidateIpAddress_WithBlockedISP_ShouldReturnForbidden")
    void testValidateIpAddress_WithBlockedISP_ShouldReturnForbidden() {
        // Arrange
        String ipAddress = "51.104.9.100";
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, false, true, "United Kingdom", "GB", "Microsoft Corporation", "Access from the ISP: Microsoft Corporation is not allowed.", HttpStatus.FORBIDDEN.value()));

        // Act
        ResponseEntity<ValidationResponse> response = fileProcessorController.validateIpAddress(ipAddress);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertFalse(response.getBody().isBlockedCountry());
        assertTrue(response.getBody().isBlockedISP());
        assertEquals("United Kingdom", response.getBody().getCountry());
        assertEquals("GB", response.getBody().getCountryCode());
        assertEquals("Microsoft Corporation", response.getBody().getIsp());
        assertEquals("Access from the ISP: Microsoft Corporation is not allowed.", response.getBody().getReason());
    }

    @Test
    @DisplayName("testValidateIpAddressDetails_WithValidIpAddress_ShouldReturnOK")
    void testValidateIpAddressDetails_WithValidIpAddress_ShouldReturnOK() {
        // Arrange
        String ipAddress = "103.24.49.0";
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(true, false, false, "Indonesia", "ID", "Politeknik Perkapalan Negeri Surabaya", "IP address is whitelisted.", HttpStatus.OK.value()));

        // Act
        ResponseEntity<String> response = fileProcessorController.validateIpAddressDetails(ipAddress);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("IP address is whitelisted.", response.getBody());
    }

    @Test
    @DisplayName("testValidateIpAddressDetails_WithBlockedCountry_ShouldReturnForbidden")
    void testValidateIpAddressDetails_WithBlockedCountry_ShouldReturnForbidden() {
        // Arrange
        String ipAddress = "8.8.8.8";
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, true, false, "United States", "US", "Google LLC", "Access from the country: United States is not allowed.", HttpStatus.FORBIDDEN.value()));

        // Act
        ResponseEntity<String> response = fileProcessorController.validateIpAddressDetails(ipAddress);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access from the country: United States is not allowed.", response.getBody());
    }

    @Test
    @DisplayName("testValidateIpAddressDetails_WithBlockedISP_ShouldReturnForbidden")
    void testValidateIpAddressDetails_WithBlockedISP_ShouldReturnForbidden() {
        // Arrange
        String ipAddress = "51.104.9.100";
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, false, true, "United Kingdom", "GB", "Microsoft Corporation", "Access from the ISP: Microsoft Corporation is not allowed.", HttpStatus.FORBIDDEN.value()));

        // Act
        ResponseEntity<String> response = fileProcessorController.validateIpAddressDetails(ipAddress);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Access from the ISP: Microsoft Corporation is not allowed.", response.getBody());
    }

    @Test
    @DisplayName("testProcessFile_WhenValidationIsSkipped_ShouldCallFileProcessingAndRequestLogService")
    void testProcessFile_WhenValidationIsSkipped_ShouldCallFileProcessingAndRequestLogService() throws Exception {
        // Arrange
        FileProcessorController fileProcessorController = new FileProcessorController(fileProcessingService, ipValidatorService, requestLogService);

        MultipartFile file = mock(MultipartFile.class);
        String ipAddress = "1.1.1.1";
        boolean skipValidation = true;

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, skipValidation);

        // Assert
        verify(fileProcessingService, times(1)).processFile(file);
        verify(ipValidatorService, never()).validateIPAddress(anyString());
        verify(requestLogService, times(1)).logRequest(
                anyString(), any(), anyInt(), anyString(), anyString(), anyString(), anyLong());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("testProcessFile_WhenValidationNotSkippedAndIpAddressIsValid_ShouldCallAllNecessaryServices")
    void testProcessFile_WhenValidationNotSkippedAndIpAddressIsValid_ShouldCallAllNecessaryServices() throws Exception {
        // Arrange
        FileProcessorController fileProcessorController = new FileProcessorController(fileProcessingService, ipValidatorService, requestLogService);

        MultipartFile file = mock(MultipartFile.class);
        String ipAddress = "1.1.1.1";
        boolean skipValidation = false;
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(true, false, false, "India", "IN", "AWS", "IP address is whitelisted", HttpStatus.OK.value()));

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, skipValidation);

        // Assert
        verify(fileProcessingService, times(1)).processFile(file);
        verify(ipValidatorService, times(1)).validateIPAddress(ipAddress);
        verify(requestLogService, times(1)).logRequest(
                anyString(), any(), anyInt(), anyString(), anyString(), anyString(), anyLong());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("testProcessFile_WhenValidationNotSkippedAndIpAddressValidationFails_ForInvalidIP_ShouldReturnErrorResponse")
    void testProcessFile_WhenValidationNotSkippedAndIpAddressValidationFails_ForInvalidIP_ShouldReturnErrorResponse() throws Exception {
        // Arrange
        FileProcessorController fileProcessorController = new FileProcessorController(fileProcessingService, ipValidatorService, requestLogService);

        MultipartFile file = mock(MultipartFile.class);
        String ipAddress = "1.1.1.1";
        boolean skipValidation = false;
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, false, false, null, null, null, "Failed to validate IP address", HttpStatus.INTERNAL_SERVER_ERROR.value()));

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, skipValidation);

        // Assert
        verify(fileProcessingService, never()).processFile(file);
        verify(ipValidatorService, times(1)).validateIPAddress(ipAddress);
        verify(requestLogService, times(1)).logRequest(
                anyString(), any(), anyInt(), anyString(), isNull(), isNull(), anyLong());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    @DisplayName("testProcessFile_WhenValidationNotSkippedAndIpAddressValidationFails_ForValidIPWithBlockedCountry_ShouldReturnForbiddenResponse")
    void testProcessFile_WhenValidationNotSkippedAndIpAddressValidationFails_ForValidIPWithBlockedCountry_ShouldReturnForbiddenResponse() throws Exception {
        // Arrange
        FileProcessorController fileProcessorController = new FileProcessorController(fileProcessingService, ipValidatorService, requestLogService);

        MultipartFile file = mock(MultipartFile.class);
        String ipAddress = "8.8.8.8";
        boolean skipValidation = false;
        when(ipValidatorService.validateIPAddress(ipAddress)).thenReturn(new ValidationResponse(false, true, false, "United States", "US", "Google LLC", "Access from the country: United States is not allowed.", HttpStatus.FORBIDDEN.value()));

        // Act
        ResponseEntity<Object> response = fileProcessorController.processFile(file, ipAddress, skipValidation);

        // Assert
        verify(fileProcessingService, never()).processFile(file);
        verify(ipValidatorService, times(1)).validateIPAddress(ipAddress);
        verify(requestLogService, times(1)).logRequest(
                anyString(), any(), anyInt(), anyString(), anyString(), anyString(), anyLong());
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("test_calculateTimeLapsed")
    void testCalculatingTimeLapsed() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusSeconds(2); // Mock a start time 10 seconds in the past
        LocalDateTime end = LocalDateTime.now();
        long expectedDuration = Duration.between(start, end).toMillis();
        FileProcessorController fileProcessorController = new FileProcessorController(fileProcessingService, ipValidatorService, requestLogService);

        // Act
        long actualDuration = fileProcessorController.calculateTimeLapsed(start);

        // Assert
        assertEquals(expectedDuration, actualDuration);
    }

    @Test
    @DisplayName("test_logRequestInDB")
    void testLoggingRequests() {
        // Arrange
        FileProcessorController fileProcessorController = new FileProcessorController(fileProcessingService, ipValidatorService, requestLogService);

        // Mock parameters
        String uri = "/api/process-file?ipAddress=127.0.0.1&skipValidation=false";
        LocalDateTime requestTimestamp = LocalDateTime.now();
        int httpStatusCode = 500;
        String ipAddress = "127.0.0.1";
        String countryCode = null;
        String requestIpProvider = null;
        long timeLapsed = 0L;

        // Act
        fileProcessorController.logRequestInDB(uri, requestTimestamp, httpStatusCode, ipAddress, countryCode, requestIpProvider, timeLapsed);

        // Assert
        verify(requestLogService, times(1)).logRequest(
                uri,
                requestTimestamp,
                httpStatusCode,
                ipAddress,
                countryCode,
                requestIpProvider,
                timeLapsed
        );
    }
}
