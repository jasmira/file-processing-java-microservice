package com.gift.and.go.file_processor.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RequestLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REQUEST_ID", columnDefinition = "BINARY(16)")
    private UUID requestId;

    @Column(name = "REQUEST_URI")
    private String requestUri;

    @Column(name = "REQUEST_TIMESTAMP")
    private LocalDateTime requestTimestamp;

    @Column(name = "HTTP_STATUS_CODE")
    private int httpResponseCode;

    @Column(name = "REQUEST_IP")
    private String requestIpAddress;

    @Column(name = "REQUEST_COUNTRY_CODE")
    private String requestCountryCode;

    @Column(name = "REQUEST_ISP")
    private String requestIpProvider;

    @Column(name = "TIME_LAPSED")
    private long timeLapsed;
}
