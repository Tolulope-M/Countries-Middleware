package com.klasha.exception;
import com.klasha.constants.AppConstants;
import com.klasha.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object,Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.builder()
                        .message(AppConstants.FAILURE_MESSAGE)
                        .errorData(getErrorsMap(errors))
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build());
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("Errors", errors);
        return errorResponse;
    }

    @ExceptionHandler(ServerSideException.class)
    public ResponseEntity<ApiResponse<Object,Object>> handleBadRequest(ServerSideException ex) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.
                        builder()
                        .message(AppConstants.FAILURE_MESSAGE)
                        .errorData(ex.getMessage())
                        .timestamp(Instant.now())
                        .status(HttpStatus.BAD_REQUEST.value())
                        .build());
    }

    @ExceptionHandler(ClientSideException.class)
    public ResponseEntity<ApiResponse<Object,Object>> handleCustomException(ClientSideException ex) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.builder()
                                .message(AppConstants.FAILURE_MESSAGE)
                                .errorData(ex.getMessage())
                                .timestamp(Instant.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .build());
    }

}
