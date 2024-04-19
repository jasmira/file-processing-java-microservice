package com.gift.and.go.file_processor.services;

import com.gift.and.go.file_processor.exceptions.FileEmptyException;
import com.gift.and.go.file_processor.models.OutcomeRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileProcessingService {

    private static final String OUTCOME_FILE_PATH = "src/main/resources/OutcomeFile.json";

    public void processFile(MultipartFile file) throws IOException {
        // Step 1: Read the content of the uploaded file
        List<String> lines = readLinesFromFile(file);
        if (lines == null || lines.isEmpty()) {
            throw new FileEmptyException("File is empty.");
        }

        // Step 2: Parse the content and extract the details needed to create the OutcomeFile.json
        List<OutcomeRecord> outcomeRecords = parseEntryFileContent(lines);

        // Step 3: Create the OutcomeFile.json with the extracted details
        createOutcomeFile(outcomeRecords);
    }

    List<String> readLinesFromFile(MultipartFile file) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        }
        return lines;
    }

    List<OutcomeRecord> parseEntryFileContent(List<String> lines) {
        List<OutcomeRecord> outcomeRecords = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty() && !line.trim().equals("\\n")) {
                String[] parts = line.split("\\|");
                String name = parts[2];
                String transport = parts[4];
                double topSpeed = Double.parseDouble(parts[6]);
                outcomeRecords.add(new OutcomeRecord(name, transport, topSpeed));
            }
        }
        return outcomeRecords;
    }

    void createOutcomeFile(List<OutcomeRecord> outcomeRecords) throws IOException {
        try (FileWriter writer = new FileWriter(OUTCOME_FILE_PATH)) {
            writer.write("[\n");
            for (int i = 0; i < outcomeRecords.size(); i++) {
                OutcomeRecord record = outcomeRecords.get(i);
                writer.write("  {\n");
                writer.write("    \"Name\": \"" + record.getName() + "\",\n");
                writer.write("    \"Transport\": \"" + record.getTransport() + "\",\n");
                writer.write("    \"Top Speed\": " + record.getTopSpeed());
                if (i < outcomeRecords.size() - 1) {
                    writer.write("\n");
                } else {
                    writer.write("\n");
                }
                writer.write("  }");
                if (i < outcomeRecords.size() - 1) {
                    writer.write(",\n");
                }
            }
            writer.write("\n]");
        }
    }
}
