package com.aicreation.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResultTest {

  @Test
  void success_WithData_ReturnsCodeZeroAndData() {
    Result<String> result = Result.success("hello");

    assertThat(result.getCode()).isZero();
    assertThat(result.getMessage()).isEqualTo(ErrorCode.SUCCESS.getMessage());
    assertThat(result.getData()).isEqualTo("hello");
  }

  @Test
  void success_WithoutData_ReturnsNullData() {
    Result<Void> result = Result.success();

    assertThat(result.getCode()).isZero();
    assertThat(result.getData()).isNull();
  }

  @Test
  void error_WithErrorCode_ReturnsCodeAndMessage() {
    Result<Void> result = Result.error(ErrorCode.NOT_FOUND);

    assertThat(result.getCode()).isEqualTo(404);
    assertThat(result.getMessage()).isEqualTo(ErrorCode.NOT_FOUND.getMessage());
    assertThat(result.getData()).isNull();
  }

  @Test
  void error_WithCustomCode_ReturnsCustomCodeAndMessage() {
    Result<Void> result = Result.error(500, "boom");

    assertThat(result.getCode()).isEqualTo(500);
    assertThat(result.getMessage()).isEqualTo("boom");
  }
}
