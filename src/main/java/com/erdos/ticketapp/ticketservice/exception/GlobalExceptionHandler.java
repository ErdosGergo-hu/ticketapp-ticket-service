package com.erdos.ticketapp.ticketservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTicketNotFound(
            TicketNotFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "Ticket not found", exception.getMessage(), request);
    }

    @ExceptionHandler(TicketsSoldOutException.class)
    public ResponseEntity<ProblemDetail> handleTicketsSoldOut(
            TicketsSoldOutException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Tickets sold out", exception.getMessage(), request);
    }

    @ExceptionHandler(TicketSaleUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleTicketSaleUnavailable(
            TicketSaleUnavailableException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Ticket sale unavailable", exception.getMessage(), request);
    }

    @ExceptionHandler(EventServiceUnavailableException.class)
    public ResponseEntity<ProblemDetail> handleEventServiceUnavailable(
            EventServiceUnavailableException exception,
            HttpServletRequest request) {
        log.warn("Event service call failed while handling {} {}: {}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getCause() == null ? exception.getMessage() : exception.getCause().toString());

        return response(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Event service unavailable",
                "Event information is temporarily unavailable. Please try again later",
                request);
    }

    @ExceptionHandler({TicketInvalidStateException.class, TicketCheckInException.class})
    public ResponseEntity<ProblemDetail> handleInvalidTicketState(
            RuntimeException exception,
            HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "Invalid ticket state", exception.getMessage(), request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLockingFailure(
            ObjectOptimisticLockingFailureException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.CONFLICT,
                "Concurrent ticket update",
                "The ticket was modified by another request. Reload it and try again",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.CONFLICT,
                "Data conflict",
                "The request conflicts with an existing ticket",
                request);
    }

    @ExceptionHandler(InvalidIdempotencyKeyException.class)
    public ResponseEntity<ProblemDetail> handleInvalidIdempotencyKey(
            InvalidIdempotencyKeyException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Invalid idempotency key",
                exception.getMessage(),
                request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Request validation failed",
                "One or more request fields are invalid",
                request);
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getConstraintViolations().forEach(violation ->
                errors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));

        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                "One or more request parameters are invalid",
                request);
        problem.setProperty("errors", errors);

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Malformed request body",
                "The request body is missing or contains invalid JSON",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter",
                "Invalid value for parameter '" + exception.getName() + "'",
                request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ProblemDetail> handleMissingHeader(
            MissingRequestHeaderException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Missing header",
                "Required header '" + exception.getHeaderName() + "' is missing",
                request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetail> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.BAD_REQUEST,
                "Missing parameter",
                "Required parameter '" + exception.getParameterName() + "' is missing",
                request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleResourceNotFound(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "Resource not found", "No endpoint exists for this path", request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        return response(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Method not allowed",
                exception.getMethod() + " is not supported for this endpoint",
                request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(
            Exception exception,
            HttpServletRequest request) {
        log.error("Unexpected error while handling {} {}",
                request.getMethod(), request.getRequestURI(), exception);

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "An unexpected error occurred",
                request);
    }

    private ResponseEntity<ProblemDetail> response(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request) {
        return ResponseEntity.status(status).body(problem(status, title, detail, request));
    }

    private ProblemDetail problem(
            HttpStatus status,
            String title,
            String detail,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
