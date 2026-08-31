package com.aicreation.common;

import lombok.Getter;

/**
 * Unified API response wrapper.
 *
 * @param <T> payload type
 */
@Getter
public class Result<T> {

  private final int code;
  private final String message;
  private final T data;

  private Result(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
  }

  public static <T> Result<T> success() {
    return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
  }

  public static <T> Result<T> success(T data) {
    return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
  }

  public static <T> Result<T> error(ErrorCode errorCode) {
    return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
  }

  public static <T> Result<T> error(int code, String message) {
    return new Result<>(code, message, null);
  }
}
