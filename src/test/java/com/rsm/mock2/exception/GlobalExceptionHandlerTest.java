package com.rsm.mock2.exception;

import com.rsm.mock2.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void testHandleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Student not found with id 1");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals("Not Found", response.getBody().error());
        assertEquals("Student not found with id 1", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void testHandleValidationException_withErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.addError(new FieldError("target", "name", "Name cannot be blank"));
        bindingResult.addError(new FieldError("target", "email", "Email format is invalid"));

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("testHandleValidationException_withErrors"), -1
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Name cannot be blank; Email format is invalid", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void testHandleValidationException_withoutErrors_fallback() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");

        MethodParameter parameter = new MethodParameter(
                this.getClass().getDeclaredMethod("testHandleValidationException_withoutErrors_fallback"), -1
        );
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Bad Request", response.getBody().error());
        assertEquals("Validation failed", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void testHandleGeneralException() {
        Exception ex = new RuntimeException("Unexpected error occurred");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneralException(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().status());
        assertEquals("Internal Server Error", response.getBody().error());
        assertEquals("Unexpected error occurred", response.getBody().message());
        assertEquals("/api/test", response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }
}
