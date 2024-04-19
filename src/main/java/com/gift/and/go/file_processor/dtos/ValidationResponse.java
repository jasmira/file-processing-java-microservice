package com.gift.and.go.file_processor.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ValidationResponse {
    private boolean valid;
    private boolean blockedCountry;
    private boolean blockedISP;
    private String country;
    private String countryCode;
    private String isp;
    private String reason;
    private int httpStatusCode;
}
