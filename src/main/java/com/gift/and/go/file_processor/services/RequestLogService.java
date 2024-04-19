package com.gift.and.go.file_processor.services;

import com.gift.and.go.file_processor.entities.RequestLogEntity;
import com.gift.and.go.file_processor.repositories.RequestLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RequestLogService {

    @Autowired
    private RequestLogRepository requestLogRepository;

    public void logRequest(String requestUri, LocalDateTime requestTimestamp, int httpResponseCode, String requestIpAddress,
                           String requestCountryCode, String requestIpProvider, long timeLapsed) {

        // Create and save request log entry
        RequestLogEntity logEntry = new RequestLogEntity();
        logEntry.setRequestUri(requestUri);
        logEntry.setRequestTimestamp(requestTimestamp);
        logEntry.setHttpResponseCode(httpResponseCode);
        logEntry.setRequestIpAddress(requestIpAddress);
        logEntry.setRequestCountryCode(requestCountryCode);
        logEntry.setRequestIpProvider(requestIpProvider);
        logEntry.setTimeLapsed(timeLapsed);

        requestLogRepository.save(logEntry);
    }
}
