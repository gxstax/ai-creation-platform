package com.aicreation.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler: converts all exceptions into a unified {@link Result}.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public Result<Void> handleBusinessException(BusinessException e) {
    log.warn("Business exception: {}", e.getMessage());
    return Result.error(e.getCode(), e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
    FieldError fieldError = e.getBindingResult().getFieldError();
    String message = fieldError == null ? ErrorCode.BAD_REQUEST.getMessage() : fieldError.getDefaultMessage();
    return Result.error(ErrorCode.BAD_REQUEST.getCode(), message);
  }

  @ExceptionHandler({BindException.class, ConstraintViolationException.class})
  public Result<Void> handleBindException(Exception e) {
    return Result.error(ErrorCode.BAD_REQUEST);
  }

  @ExceptionHandler(Exception.class)
  public Result<Void> handleException(Exception e) {
    log.error("Unexpected error", e);
    return Result.error(ErrorCode.INTERNAL_ERROR);
  }
}
