package com.print.storage.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalUploadServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void store_shouldSaveFileWithSanitizedName() throws IOException {
        LocalUploadService service = new LocalUploadService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "origin name.pdf",
                "application/pdf",
                "dummy".getBytes()
        );

        String stored = service.store("docs", "invoice 2026", file);

        assertEquals("invoice_2026.pdf", stored);
        assertTrue(Files.exists(tempDir.resolve("docs").resolve(stored)));
    }

    @Test
    void store_shouldThrow_whenFileIsEmpty() throws IOException {
        LocalUploadService service = new LocalUploadService(tempDir.toString());
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        assertThrows(IOException.class, () -> service.store("docs", "empty", file));
    }

    @Test
    void delete_shouldNotThrow_whenFileMissing() throws IOException {
        LocalUploadService service = new LocalUploadService(tempDir.toString());

        service.delete("missing.txt");
    }

    @Test
    void delete_shouldRemoveFile_whenPresentAtRoot() throws IOException {
        LocalUploadService service = new LocalUploadService(tempDir.toString());
        Path filePath = tempDir.resolve("to-delete.txt");
        Files.writeString(filePath, "content");
        assertTrue(Files.exists(filePath));

        service.delete("to-delete.txt");

        assertTrue(Files.notExists(filePath));
    }

    @Test
    void absolutePathFor_shouldReturnNull_whenInputNull() throws IOException {
        LocalUploadService service = new LocalUploadService(tempDir.toString());
        assertNull(service.absolutePathFor(null));
    }

    @Test
    void absolutePathFor_shouldReturnAbsolutePath() throws IOException {
        LocalUploadService service = new LocalUploadService(tempDir.toString());
        String absolute = service.absolutePathFor("file.txt");

        assertNotNull(absolute);
        assertTrue(absolute.endsWith("file.txt"));
    }
}
