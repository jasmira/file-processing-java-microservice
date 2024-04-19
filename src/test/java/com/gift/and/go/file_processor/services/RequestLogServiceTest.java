package com.gift.and.go.file_processor.services;

import com.gift.and.go.file_processor.entities.RequestLogEntity;
import com.gift.and.go.file_processor.repositories.RequestLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RequestLogServiceTest {

    @Mock
    private RequestLogRepository requestLogRepository;

    @InjectMocks
    private RequestLogService requestLogService;

    @Test
    @DisplayName("test_logRequest_sendingCorrectParamsToSaveInDB")
    public void testLogRequest() {
        // Mock data
        String requestUri = "/example";
        LocalDateTime requestTimestamp = LocalDateTime.now();
        int httpResponseCode = 200;
        String requestIpAddress = "192.168.1.1";
        String requestCountryCode = "US";
        String requestIpProvider = "Provider";
        long timeLapsed = 1000;

        // Call the method
        requestLogService.logRequest(requestUri, requestTimestamp, httpResponseCode,
                requestIpAddress, requestCountryCode, requestIpProvider, timeLapsed);

        // Verify that save method is called with the correct arguments
        ArgumentCaptor<RequestLogEntity> captor = ArgumentCaptor.forClass(RequestLogEntity.class);
        verify(requestLogRepository).save(captor.capture());

        // Assert the captured argument
        RequestLogEntity capturedLogEntry = captor.getValue();
        assertEquals(requestUri, capturedLogEntry.getRequestUri());
        assertEquals(requestTimestamp, capturedLogEntry.getRequestTimestamp());
        assertEquals(httpResponseCode, capturedLogEntry.getHttpResponseCode());
        assertEquals(requestIpAddress, capturedLogEntry.getRequestIpAddress());
        assertEquals(requestCountryCode, capturedLogEntry.getRequestCountryCode());
        assertEquals(requestIpProvider, capturedLogEntry.getRequestIpProvider());
        assertEquals(timeLapsed, capturedLogEntry.getTimeLapsed());
    }
}
