package com.aicreation.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Unified error codes for API responses.
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

  SUCCESS(0, "success"),
  BAD_REQUEST(400, "参数错误"),
  UNAUTHORIZED(401, "未认证"),
  FORBIDDEN(403, "无权限"),
  NOT_FOUND(404, "资源不存在"),
  INTERNAL_ERROR(500, "系统繁忙"),
  AI_SERVICE_ERROR(1001, "AI 服务异常");

  private final int code;
  private final String message;
}
