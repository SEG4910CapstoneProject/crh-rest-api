package me.t65.reportgenapi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class RestControllerAdvice {

    // Exception Handling
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.badRequest()
                .body(
                        "Unable to parse param '"
                                + ex.getName()
                                + "'. Received '"
                                + ex.getName()
                                + ".");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.badRequest().build();
    }
}
