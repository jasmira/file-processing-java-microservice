package com.gift.and.go.file_processor.services;

import com.gift.and.go.file_processor.dtos.IPAPIResponse;
import com.gift.and.go.file_processor.dtos.ValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IPValidatorService {

    private final RestTemplate restTemplate;

    @Autowired
    public IPValidatorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ValidationResponse validateIPAddress(String ipAddress) {
        // Example: http://ip-api.com/json/24.48.0.1?fields=status,country,countryCode,isp,query
        String apiUrl = "http://ip-api.com/json/" + ipAddress + "?fields=status,country,countryCode,isp,org,query";
        IPAPIResponse response = restTemplate.getForObject(apiUrl, IPAPIResponse.class);

        String country = null;
        String isp = null;
        String org;
        String countryCode = null;
        int httpStatusCode;
        String message;

        if (response != null && "success".equals(response.getStatus())) {
            country = response.getCountry();
            countryCode = response.getCountryCode();
            isp = response.getIsp();
            org = response.getOrg();

            if (isBlockedCountry(country)) {
                httpStatusCode = HttpStatus.FORBIDDEN.value();
                message = "Access from the country: " + country + " is not allowed.";
                return new ValidationResponse(false, true, false, country, countryCode, isp, message, httpStatusCode);
            } else if (isBlockedISP(org)) {
                httpStatusCode = HttpStatus.FORBIDDEN.value();
                message = "Access from the ISP: " + isp + " is not allowed.";
                return new ValidationResponse(false, false, true, country, countryCode, isp, message, httpStatusCode);
            } else {
                httpStatusCode = HttpStatus.OK.value();
                message = "IP address is whitelisted.";
                return new ValidationResponse(true, false, false, country, countryCode, isp, message, httpStatusCode);
            }
        } else {
            httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            message = "Failed to validate IP address.";
            return new ValidationResponse(false, false, false, country, "INVALID", "INVALID", message, httpStatusCode);
        }
    }

    private boolean isBlockedCountry(String country) {
        return country.equalsIgnoreCase("CHINA") || country.equalsIgnoreCase("SPAIN") || country.equalsIgnoreCase("UNITED STATES");
    }

    private boolean isBlockedISP(String isp) {
        return isp.toLowerCase().contains("aws") || isp.toLowerCase().contains("google cloud") || isp.toLowerCase().contains("microsoft azure");
    }
}
