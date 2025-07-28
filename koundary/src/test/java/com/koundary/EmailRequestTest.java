package com.koundary;

import com.koundary.domain.verification.dto.EmailRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmailRequestTest {

    @Test
    public void testGetterSetter() {
        EmailRequest dto = new EmailRequest();
        dto.setEmail("test@example.com");

        assertEquals("test@example.com", dto.getEmail());
    }
}
