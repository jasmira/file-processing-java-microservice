package com.gift.and.go.file_processor.controllers;

import com.gift.and.go.file_processor.dtos.ValidationResponse;
import com.gift.and.go.file_processor.services.FileProcessingService;
import com.gift.and.go.file_processor.services.IPValidatorService;
import com.gift.and.go.file_processor.services.RequestLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileProcessorController {

    private final FileProcessingService fileProcessingService;

    private final IPValidatorService ipValidatorService;

    private final RequestLogService requestLogService;

    @Autowired
    public FileProcessorController(FileProcessingService fileProcessingService, IPValidatorService ipValidatorService, RequestLogService requestLogService) {
        this.fileProcessingService = fileProcessingService;
        this.ipValidatorService = ipValidatorService;
        this.requestLogService = requestLogService;
    }

    @PostMapping(value = "/process-file", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> processFile(@RequestParam("file") MultipartFile file,
                                         @RequestParam("ipAddress") String ipAddress,
                                         @RequestParam(value = "skipValidation", required = false, defaultValue = "false") boolean skipValidation) {

        LocalDateTime requestTimestamp = LocalDateTime.now(); // Capture the start time
        long timeLapsed;
        String uri;
        int httpStatusCode = 0;
        String countryCode = null;
        String ipProvider = null;

        try {
            if (skipValidation) {
                uri = "/api/process-file?ipAddress=" + ipAddress + "&skipValidation=true";

                fileProcessingService.processFile(file);

                httpStatusCode = HttpStatus.OK.value();
                timeLapsed = calculateTimeLapsed(requestTimestamp);

                logRequestInDB(uri, requestTimestamp, httpStatusCode, ipAddress,
                        "NOT_VALIDATED", "NOT_VALIDATED", timeLapsed);
            } else {
                uri = "/api/process-file?ipAddress=" + ipAddress + "&skipValidation=false";

                // Validate the IP address
                ValidationResponse validationResponse = validateIpAddress(ipAddress).getBody();

                if (validationResponse != null) {
                    countryCode = validationResponse.getCountryCode();
                    ipProvider = validationResponse.getIsp();
                    httpStatusCode = validationResponse.getHttpStatusCode();
                }

                // If Ip Address is not whitelisted, do not process the file
                if (httpStatusCode != HttpStatus.OK.value()) {
                    timeLapsed = calculateTimeLapsed(requestTimestamp);

                    logRequestInDB(uri, requestTimestamp, httpStatusCode, ipAddress,
                            countryCode, ipProvider, timeLapsed);

                    return ResponseEntity.status(httpStatusCode)
                            .body(validationResponse);
                }
                // If Ip Address is whitelisted, process the file
                else {
                    fileProcessingService.processFile(file);
                    timeLapsed = calculateTimeLapsed(requestTimestamp);

                    logRequestInDB(uri, requestTimestamp, httpStatusCode, ipAddress,
                            countryCode, ipProvider, timeLapsed);
                }
            }

            // Read the generated OutcomeFile.json
            File outcomeFile = new File("src/main/resources/OutcomeFile.json");
            InputStreamResource resource = new InputStreamResource(new FileInputStream(outcomeFile));

            // Return the OutcomeFile.json as a response
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .contentLength(outcomeFile.length())
                    .body(resource);
        } catch (Exception e) {
            timeLapsed = calculateTimeLapsed(requestTimestamp);
            String requestUri = getURIString();

            if(requestUri.contains("skipValidation=true")) {
                logRequestInDB(requestUri, requestTimestamp, HttpStatus.INTERNAL_SERVER_ERROR.value(), ipAddress,
                        "NOT_VALIDATED", "NOT_VALIDATED", timeLapsed);
            } else {
                logRequestInDB(requestUri, requestTimestamp, HttpStatus.INTERNAL_SERVER_ERROR.value(), ipAddress,
                        countryCode, ipProvider, timeLapsed);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the file: " + e.getMessage());
        }
    }

    @GetMapping("/validate-ip")
    public ResponseEntity<ValidationResponse> validateIpAddress(@RequestParam("ipAddress") String ipAddress) {
        ValidationResponse validationResponse = ipValidatorService.validateIPAddress(ipAddress);

        int httpStatusCode = validationResponse.getHttpStatusCode();
        return ResponseEntity.status(httpStatusCode)
                .body(validationResponse);
    }

    @GetMapping("/validate-ip/details")
    public ResponseEntity<String> validateIpAddressDetails(@RequestParam("ipAddress") String ipAddress) {
        ValidationResponse validationResponse = ipValidatorService.validateIPAddress(ipAddress);

        int httpStatusCode = validationResponse.getHttpStatusCode();
        String errorMessage = validationResponse.getReason();
        return ResponseEntity.status(httpStatusCode)
                .body(errorMessage);
    }

    /**
     * Method to get URI string for requests failing with 500 error to save in DB.
     * @return String.
     */
    private static String getURIString() {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String requestUri = request.getRequestURI();

        // Get the parameter map
        Map<String, String[]> parameterMap = request.getParameterMap();

        // Build the query string from the parameter map
        StringBuilder queryStringBuilder = new StringBuilder();
        parameterMap.forEach((param, values) -> {
            for (String value : values) {
                if (!queryStringBuilder.isEmpty()) {
                    queryStringBuilder.append('&');
                }
                queryStringBuilder.append(param).append('=').append(value);
            }
        });

        // Get the query string
        String queryString = queryStringBuilder.toString();

        // Append the query string to the request URL if it exists
        if (!queryString.isEmpty()) {
            requestUri += "?" + queryString;
        }
        return requestUri;
    }

    /**
     * Method to call Request Log Service to save the Log entries for each request.
     * @param uri is the URI of the request.
     * @param requestTimestamp is the timestamp when the request reached the application.
     * @param httpStatusCode is the response code of the request.
     * @param ipAddress is the IP Address passed in the request.
     * @param requestCountryCode is the Country Code of the IP Address.
     * @param requestIpProvider is the ISP of the IP Address.
     * @param timeLapsed The time taken (in milliseconds) to complete the request.
     */
    void logRequestInDB(String uri, LocalDateTime requestTimestamp, int httpStatusCode, String ipAddress, String requestCountryCode, String requestIpProvider, long timeLapsed) {
        requestLogService.logRequest(uri, requestTimestamp, httpStatusCode, ipAddress, requestCountryCode, requestIpProvider, timeLapsed);
    }

    /**
     * Method to calculate the time lapsed.
     * @param requestTimestamp is the timestamp when the request reached the application.
     * @return The time taken (in milliseconds) to complete the request.
     */
    long calculateTimeLapsed(LocalDateTime requestTimestamp) {
        LocalDateTime endTime = LocalDateTime.now(); // Capture the end time
        Duration duration = Duration.between(requestTimestamp, endTime);
        return duration.toMillis();
    }
}
