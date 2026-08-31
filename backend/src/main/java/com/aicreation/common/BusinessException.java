package com.aicreation.common;

import lombok.Getter;

/**
 * Exception for expected business failures, handled by {@link GlobalExceptionHandler}.
 */
@Getter
public class BusinessException extends RuntimeException {

  private final int code;

  public BusinessException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.code = errorCode.getCode();
  }

  public BusinessException(ErrorCode errorCode, String message) {
    super(message);
    this.code = errorCode.getCode();
  }
}
