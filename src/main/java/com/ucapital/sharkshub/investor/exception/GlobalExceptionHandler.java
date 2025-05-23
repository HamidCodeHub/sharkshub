package com.ucapital.sharkshub.investor.exception;

import com.mongodb.MongoWriteException;
import com.mongodb.DuplicateKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    private static class ApiError {
        private final HttpStatus status;
        private final String message;
        private final String error;
        private final Instant timestamp;
        private Object details;

        public ApiError(HttpStatus status, String message, String error) {
            this.status = status;
            this.message = message;
            this.error = error;
            this.timestamp = Instant.now();
        }

        public ApiError(HttpStatus status, String message, String error, Object details) {
            this(status, message, error);
            this.details = details;
        }

        public HttpStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getError() {
            return error;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public Object getDetails() {
            return details;
        }
    }


    @ExceptionHandler(InvestorValidationException.class)
    public ResponseEntity<Object> handleInvestorValidation(
            InvestorValidationException ex, WebRequest request) {

        logger.error("Validation exception: {}", ex.getFormattedMessage());

        Map<String, Object> details = new HashMap<>();
        details.put("investorName", ex.getInvestorName());
        details.put("investorIndex", ex.getInvestorIndex());

        if (ex.hasValidationErrors()) {
            List<Map<String, Object>> errors = new ArrayList<>();
            ex.getValidationErrors().forEach(error -> {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("field", error.getFieldName());
                errorMap.put("message", error.getErrorMessage());
                errorMap.put("rejectedValue", error.getRejectedValue());
                errors.add(errorMap);
            });
            details.put("validationErrors", errors);
        }

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Investor validation failed",
                ex.getMessage(),
                details
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {

        List<Map<String, String>> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            Map<String, String> error = new HashMap<>();
            error.put("path", violation.getPropertyPath().toString());
            error.put("message", violation.getMessage());
            error.put("invalidValue", violation.getInvalidValue() != null ?
                    violation.getInvalidValue().toString() : "null");
            errors.add(error);
        }

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                "Constraint validation failed",
                errors
        );

        logger.error("Constraint violation: {}", errors);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {

        List<Map<String, String>> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("field", error.getField());
            errorDetails.put("message", error.getDefaultMessage());
            errorDetails.put("rejectedValue", error.getRejectedValue() != null ?
                    error.getRejectedValue().toString() : "null");
            errors.add(errorDetails);
        }

        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("object", error.getObjectName());
            errorDetails.put("message", error.getDefaultMessage());
            errors.add(errorDetails);
        }

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Validation error",
                "Method argument validation failed",
                errors
        );

        logger.error("Method argument validation error: {}", errors);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> handleDuplicateKey(DuplicateKeyException ex, WebRequest request) {
        String error = "Database constraint violation: " + ex.getMessage();
        logger.error("Duplicate key exception: {}", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT,
                "The provided data conflicts with existing records",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(MongoWriteException.class)
    public ResponseEntity<Object> handleMongoWriteException(MongoWriteException ex, WebRequest request) {
        String error = "Database error occurred: " + ex.getMessage();
        logger.error("MongoDB write exception: {}", ex.getMessage(), ex);

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while writing to the database",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDataAccessException(DataAccessException ex, WebRequest request) {
        String error = "Database access error: " + ex.getMessage();
        logger.error("Data access exception: {}", ex.getMessage(), ex);

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while accessing the database",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, WebRequest request) {

        String error = "File upload size exceeded: " + ex.getMessage();
        logger.error("Max upload size exceeded: {}", ex.getMessage());

        ApiError apiError = new ApiError(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "The uploaded file exceeds the maximum allowed size",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(IOException.class)
    public ResponseEntity<Object> handleIOException(IOException ex, WebRequest request) {
        String error = "File operation error: " + ex.getMessage();
        logger.error("IO exception: {}", ex.getMessage(), ex);

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An error occurred while processing the file",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, WebRequest request) {

        String error = String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), ex.getRequiredType().getSimpleName());
        logger.error("Type mismatch: {}", error);

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Type mismatch for request parameter",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest request) {
        String error = "Unexpected error occurred: " + ex.getMessage();
        logger.error("Unhandled exception: ", ex);

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, WebRequest request) {

        String error = String.format("No handler found for %s %s", ex.getHttpMethod(), ex.getRequestURL());
        logger.error("No handler found: {}", error);

        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND,
                "The requested resource could not be found",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, WebRequest request) {

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getMethod());
        builder.append(" method is not supported for this request. Supported methods are ");
        ex.getSupportedHttpMethods().forEach(t -> builder.append(t).append(" "));

        ApiError apiError = new ApiError(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                builder.toString()
        );

        logger.error("Method not supported: {}", builder.toString());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, WebRequest request) {

        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));

        ApiError apiError = new ApiError(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type",
                builder.substring(0, builder.length() - 2)
        );

        logger.error("Media type not supported: {}", builder.toString());
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, WebRequest request) {

        String error = ex.getRequestPartName() + " part is missing";
        logger.error("Missing request part: {}", error);

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Missing request part",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";
        logger.error("Missing parameter: {}", error);

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Missing request parameter",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {

        String error = "Malformed JSON request: " + ex.getMessage();
        logger.error("Message not readable: {}", error);

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                error
        );

        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }


    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(
            BindException ex, WebRequest request) {

        List<Map<String, String>> errors = new ArrayList<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("field", error.getField());
            errorDetails.put("message", error.getDefaultMessage());
            errorDetails.put("rejectedValue", error.getRejectedValue() != null ?
                    error.getRejectedValue().toString() : "null");
            errors.add(errorDetails);
        });

        ex.getBindingResult().getGlobalErrors().forEach(error -> {
            Map<String, String> errorDetails = new HashMap<>();
            errorDetails.put("object", error.getObjectName());
            errorDetails.put("message", error.getDefaultMessage());
            errors.add(errorDetails);
        });

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST,
                "Binding error",
                "Error binding request parameters",
                errors
        );

        logger.error("Binding exception: {}", errors);
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }
}