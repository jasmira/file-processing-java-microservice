package com.gift.and.go.file_processor.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class OutcomeRecord {
    private final String name;
    private final String transport;
    private final double topSpeed;
}
