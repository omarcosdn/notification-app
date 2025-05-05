package com.github.omarcosdn.notification.infrastructure.config;

import com.github.omarcosdn.notification.shared.exceptions.DomainException;
import com.github.omarcosdn.notification.shared.exceptions.NotFoundException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<ApiError> handleException(final Exception ex) {
    var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
    return ResponseEntity.status(httpStatus).body(ApiError.from(ex, httpStatus));
  }

  @ExceptionHandler(value = NotFoundException.class)
  public ResponseEntity<ApiError> handleNotFoundException(NotFoundException ex) {
    var httpStatus = HttpStatus.NOT_FOUND;
    return ResponseEntity.status(httpStatus).body(ApiError.from(ex, httpStatus));
  }

  @ExceptionHandler(value = DomainException.class)
  public ResponseEntity<ApiError> handleDomainException(DomainException ex) {
    var httpStatus = HttpStatus.UNPROCESSABLE_ENTITY;
    return ResponseEntity.status(httpStatus).body(ApiError.from(ex, httpStatus));
  }

  public record ApiError(String code, String message, Instant timestamp) {

    static ApiError from(final Exception e, final HttpStatus httpStatus) {
      logger.error(e.getMessage(), e);
      return new ApiError(
          httpStatus.getReasonPhrase(),
          "An unexpected error occurred while processing your request.",
          Instant.now());
    }

    static ApiError from(final DomainException e, final HttpStatus httpStatus) {
      logger.warn(e.getMessage(), e);
      return new ApiError(httpStatus.getReasonPhrase(), e.getMessage(), Instant.now());
    }
  }
}
