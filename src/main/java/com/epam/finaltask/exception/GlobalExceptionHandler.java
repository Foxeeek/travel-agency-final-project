package com.epam.finaltask.exception;

import com.epam.finaltask.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>("NOT_FOUND", e.getMessage(), null));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(NoResourceFoundException e,
                                                                   HttpServletRequest request,
                                                                   HttpServletResponse response) throws IOException {

        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("NOT_FOUND", "Resource not found", null));
        }
        response.sendError(HttpStatus.NOT_FOUND.value());
        return null;
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiResponse<>("METHOD_NOT_ALLOWED", "Method not allowed", null));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>("CONFLICT", e.getMessage(), null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("BAD_REQUEST", e.getMessage(), null));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("BAD_REQUEST", "Required parameter " + e.getParameterName() + " is missing", null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("BAD_REQUEST", "Required parameter " + e.getName() + " has invalid value: '" + e.getValue() + "'", null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("BAD_REQUEST", "Malformed request body", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("INTERNAL_SERVER_ERROR", "Internal server error", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        List<FieldError> errors = e.getBindingResult().getFieldErrors();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("BAD_REQUEST",
                        errors.stream().map(error -> error.getField() + ": " + error.getDefaultMessage()).collect(Collectors.joining("; ")),
                        null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>("UNAUTHORIZED", "Invalid username or password", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
        log.warn("Access denied for user '{}' to endpoint '{}'",
                SecurityContextHolder.getContext().getAuthentication().getName(),
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>("FORBIDDEN", "Access denied", null));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        log.warn("Content type is not supported: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(new ApiResponse<>("UNSUPPORTED_MEDIA_TYPE", "Content type is not supported", null));
    }


    @ControllerAdvice(basePackages = "com.epam.finaltask.controller")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public class PageExceptionHandler {
        private final MessageSource messageSource;

        public PageExceptionHandler(MessageSource messageSource) {
            this.messageSource = messageSource;
        }

        private static final Logger log = LoggerFactory.getLogger(PageExceptionHandler.class);

        @ExceptionHandler(IllegalArgumentException.class)
        public ModelAndView handleBadRequest(IllegalArgumentException e) {
            return errorPage(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ModelAndView handleTypeMismatch(MethodArgumentTypeMismatchException e) {
            log.warn("Invalid value for parameter {}", e.getName());
            return errorPage(HttpStatus.BAD_REQUEST, msg("error.param.invalid"));
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ModelAndView handleAccessDenied(AccessDeniedException e, HttpServletRequest request) {
            log.warn("Access denied for user '{}' to page '{}'",
                    SecurityContextHolder.getContext().getAuthentication().getName(),
                    request.getRequestURI());
            return errorPage(HttpStatus.FORBIDDEN, msg("error.access.denied"));
        }

        @ExceptionHandler(Exception.class)
        public ModelAndView handleOther(Exception e) {
            log.error("Unexpected error", e);
            return errorPage(HttpStatus.INTERNAL_SERVER_ERROR, msg("error.default"));
        }

        private ModelAndView errorPage(HttpStatus status, String message) {
            ModelAndView mav = new ModelAndView("error");
            mav.setStatus(status);
            mav.addObject("status", status.value());
            mav.addObject("message", message);
            return mav;
        }

        private String msg(String key) {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        }

        private String exceptionMsg(LocalizedException e) {
            if (e.getKey() == null) {
                return e.getMessage();
            }
            return messageSource.getMessage(e.getKey(), e.getArgs(), LocaleContextHolder.getLocale());
        }
    }


}
