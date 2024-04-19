package com.gift.and.go.file_processor.services;

import com.gift.and.go.file_processor.entities.RequestLogEntity;
import com.gift.and.go.file_processor.repositories.RequestLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class RequestLogServiceIntegrationTest {

    @Autowired
    private RequestLogRepository requestLogRepository;

    @Autowired
    private RequestLogService requestLogService;

    @Test
    @DisplayName("test_logRequest_isActuallySavingInDB")
    public void testLogRequestInDB() {
        // Mock data
        String requestUri = "/example";
        LocalDateTime requestTimestamp = LocalDateTime.now();
        int httpResponseCode = 200;
        String requestIpAddress = "192.168.1.1";
        String requestCountryCode = "UK";
        String requestIpProvider = "IP_Provider";
        long timeLapsed = 1000;

        // Call the method
        requestLogService.logRequest(requestUri, requestTimestamp, httpResponseCode,
                requestIpAddress, requestCountryCode, requestIpProvider, timeLapsed);

        // Fetch the saved entity from the database
        List<RequestLogEntity> savedLogs = requestLogRepository.findAll();
        assertNotNull(savedLogs);
        assertEquals(1, savedLogs.size());

        // Verify the saved values
        RequestLogEntity savedLog = savedLogs.get(0);
        assertEquals(requestUri, savedLog.getRequestUri());
        assertEquals(requestTimestamp.truncatedTo(ChronoUnit.SECONDS), savedLog.getRequestTimestamp().truncatedTo(ChronoUnit.SECONDS));
        assertEquals(httpResponseCode, savedLog.getHttpResponseCode());
        assertEquals(requestIpAddress, savedLog.getRequestIpAddress());
        assertEquals(requestCountryCode, savedLog.getRequestCountryCode());
        assertEquals(requestIpProvider, savedLog.getRequestIpProvider());
        assertEquals(timeLapsed, savedLog.getTimeLapsed());
    }
}
