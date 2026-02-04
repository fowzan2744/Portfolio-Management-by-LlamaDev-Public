package com.backend.Portfolio_Backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HashServiceTest {

    @InjectMocks
    private HashService hashService;

    // ===================== sha256 Tests =====================

    @Test
    void testSha256_BasicString() {
        // Arrange
        String input = "hello";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", result);
    }

    @Test
    void testSha256_EmptyString() {
        // Arrange
        String input = "";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        // SHA-256 of empty string
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", result);
    }

    @Test
    void testSha256_LongString() {
        // Arrange
        String input = "The quick brown fox jumps over the lazy dog";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        assertEquals("d7a8fbb307d7809469ca9abcb0082e4f8d5651e46d3cdb762d02d0bf37c9e592", result);
    }

    @Test
    void testSha256_SpecialCharacters() {
        // Arrange
        String input = "!@#$%^&*()_+-=[]{}|;':\",./<>?";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.length()); // SHA-256 produces 64 hex characters
        assertTrue(result.matches("[a-f0-9]{64}")); // Only hex characters
    }

    @Test
    void testSha256_UnicodeCharacters() {
        // Arrange
        String input = "你好世界🌍";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.length());
        assertTrue(result.matches("[a-f0-9]{64}"));
    }

    @Test
    void testSha256_Numbers() {
        // Arrange
        String input = "12345";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.length());
        assertTrue(result.matches("[a-f0-9]{64}"));
    }

    @Test
    void testSha256_WithSpaces() {
        // Arrange
        String input = "hello world";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", result);
    }

    @Test
    void testSha256_CaseSensitive() {
        // Arrange
        String input1 = "Hello";
        String input2 = "hello";

        // Act
        String result1 = hashService.sha256(input1);
        String result2 = hashService.sha256(input2);

        // Assert
        assertNotEquals(result1, result2); // SHA-256 is case sensitive
    }

    @Test
    void testSha256_DifferentInputsDifferentHashes() {
        // Arrange
        String input1 = "password123";
        String input2 = "password124";

        // Act
        String result1 = hashService.sha256(input1);
        String result2 = hashService.sha256(input2);

        // Assert
        assertNotEquals(result1, result2);
    }

    @Test
    void testSha256_SameInputSameHash() {
        // Arrange
        String input = "teststring";

        // Act
        String result1 = hashService.sha256(input);
        String result2 = hashService.sha256(input);

        // Assert
        assertEquals(result1, result2); // Same input should produce same hash
    }

    @Test
    void testSha256_ProducesLowercaseHex() {
        // Arrange
        String input = "TEST";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertEquals(result, result.toLowerCase());
        assertTrue(result.matches("[a-f0-9]{64}"));
    }

    @Test
    void testSha256_LongMultilineString() {
        // Arrange
        String input = "Line 1\nLine 2\nLine 3\nLine 4";

        // Act
        String result = hashService.sha256(input);

        // Assert
        assertNotNull(result);
        assertEquals(64, result.length());
        assertTrue(result.matches("[a-f0-9]{64}"));
    }

    @Test
    void testSha256_Returns64Characters() {
        // Arrange
        String input = "test";

        // Act
        String result = hashService.sha256(input);

        // Assert
        // SHA-256 always produces 64 hexadecimal characters
        assertEquals(64, result.length());
    }

    @Test
    void testSha256_NullInput_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            hashService.sha256(null);
        });
    }
}
