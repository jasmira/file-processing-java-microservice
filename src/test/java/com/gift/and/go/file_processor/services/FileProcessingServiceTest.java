package com.gift.and.go.file_processor.services;

import com.gift.and.go.file_processor.exceptions.FileEmptyException;
import com.gift.and.go.file_processor.models.OutcomeRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class FileProcessingServiceTest {

    @InjectMocks
    private FileProcessingService fileProcessingService;

    private static final String OUTCOME_FILE_PATH = "src/main/resources/OutcomeFile.json";

    @Test
    @DisplayName("test_processFile_withCorrectFile")
    void testProcessFile() throws IOException {
        // Create a spy of FileProcessingService
        FileProcessingService fileProcessingServiceSpy = spy(new FileProcessingService());

        // Prepare test file
        String fileContent = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n" + "\\n\n" +
                "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5\n";
        MultipartFile multipartFile = new MockMultipartFile("file", "filename.txt", "text/plain", fileContent.getBytes());

        // Call the processFile method with the dummy multipart file
        fileProcessingServiceSpy.processFile(multipartFile);

        // Verify that readLinesFromFile, parseEntryFileContent, and createOutcomeFile were called at least once
        verify(fileProcessingServiceSpy, atLeastOnce()).readLinesFromFile(any());
        verify(fileProcessingServiceSpy, atLeastOnce()).parseEntryFileContent(any());
        verify(fileProcessingServiceSpy, atLeastOnce()).createOutcomeFile(any());
    }

    @Test
    @DisplayName("test_processFile_withEmptyFile")
    void testProcessFileWithEmptyFile() throws IOException {
        // Create a spy of FileProcessingService
        FileProcessingService fileProcessingServiceSpy = spy(new FileProcessingService());

        // Prepare an empty test file
        MultipartFile emptyFile = new MockMultipartFile("emptyFile", "empty.txt", "text/plain", "".getBytes());

        // Verify that the FileEmptyException is thrown with the correct message
        assertThrows(FileEmptyException.class, () -> fileProcessingServiceSpy.processFile(emptyFile), "File is empty.");
    }

    @Test
    @DisplayName("test_readLinesFromFile")
    public void testReadLinesFromFile() throws IOException {
        // Prepare test file content
        String fileContent = "18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1\n" +
                "\\n\n" +
                "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5";

        // Create a mock MultipartFile object with the test file content
        MockMultipartFile multipartFile = new MockMultipartFile("file", "filename.txt", "text/plain", fileContent.getBytes());

        // Call the method to be tested
        List<String> lines = new FileProcessingService().readLinesFromFile(multipartFile);

        // Check the result
        assertEquals(3, lines.size());
        assertEquals("18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1", lines.get(0));
        assertEquals("\\n", lines.get(1));
        assertEquals("3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5", lines.get(2));
    }

    @Test
    @DisplayName("test_parseEntryFileContent")
    public void testParseEntryFileContent() {
        // Prepare input data
        List<String> lines = List.of("18148426-89e1-11ee-b9d1-0242ac120002|1X1D14|John Smith|Likes Apricots|Rides A Bike|6.2|12.1", "3ce2d17b-e66a-4c1e-bca3-40eb1c9222c7|2X2D24|Mike Smith|Likes Grape|Drives an SUV|35.0|95.5");

        // Call the method
        List<OutcomeRecord> outcomeRecords = fileProcessingService.parseEntryFileContent(lines);

        // Verify the outcomeRecords
        assertEquals(2, outcomeRecords.size());
        assertEquals("John Smith", outcomeRecords.get(0).getName());
        assertEquals("Rides A Bike", outcomeRecords.get(0).getTransport());
        assertEquals(12.1, outcomeRecords.get(0).getTopSpeed());

        assertEquals("Mike Smith", outcomeRecords.get(1).getName());
        assertEquals("Drives an SUV", outcomeRecords.get(1).getTransport());
        assertEquals(95.5, outcomeRecords.get(1).getTopSpeed());
    }

    @Test
    @DisplayName("test_createOutcomeFile")
    public void testCreateOutcomeFile() throws IOException {
        // Prepare dummy outcome records
        List<OutcomeRecord> outcomeRecords = createDummyOutcomeRecords();

        // Call the method to be tested
        fileProcessingService.createOutcomeFile(outcomeRecords);

        // Read the content of the created file
        List<String> fileContent = readOutcomeFileContent();

        // Check if the content is as expected
        assertEquals("[", fileContent.get(0));
        assertEquals("{", fileContent.get(1));
        assertEquals("\"Name\": \"John Smith\",", fileContent.get(2));
        assertEquals("\"Transport\": \"Rides A Bike\",", fileContent.get(3));
        assertEquals("\"Top Speed\": 12.1", fileContent.get(4));
        assertEquals("},", fileContent.get(5));
        assertEquals("{", fileContent.get(6));
        assertEquals("\"Name\": \"Mike Smith\",", fileContent.get(7));
        assertEquals("\"Transport\": \"Drives an SUV\",", fileContent.get(8));
        assertEquals("\"Top Speed\": 95.5", fileContent.get(9));
        assertEquals("}", fileContent.get(10));
        assertEquals("]", fileContent.get(11));
    }

    /**
     * Method that creates dummy List of OutcomeRecord.
     * @return List<OutcomeRecord>
     */
    private List<OutcomeRecord> createDummyOutcomeRecords() {
        List<OutcomeRecord> outcomeRecords = new ArrayList<>();
        outcomeRecords.add(new OutcomeRecord("John Smith", "Rides A Bike", 12.1));
        outcomeRecords.add(new OutcomeRecord("Mike Smith", "Drives an SUV", 95.5));
        return outcomeRecords;
    }

    /**
     * Method that reads a file.
     * @return List<String>
     * @throws IOException is thrown is case of any issue while file reading.
     */
    private List<String> readOutcomeFileContent() throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ResourceUtils.getFile(OUTCOME_FILE_PATH)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.trim());
            }
        }
        return lines;
    }
}
