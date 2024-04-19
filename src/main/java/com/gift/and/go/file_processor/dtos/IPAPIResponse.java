package com.gift.and.go.file_processor.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IPAPIResponse {

    @JsonProperty("query")
    private String query;

    @JsonProperty("status")
    private String status;

    @JsonProperty("country")
    private String country;

    @JsonProperty("countryCode")
    private String countryCode;

    @JsonProperty("isp")
    private String isp;

    @JsonProperty("org")
    private String org;
}
