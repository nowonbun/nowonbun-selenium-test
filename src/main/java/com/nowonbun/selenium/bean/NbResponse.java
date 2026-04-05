package com.nowonbun.selenium.bean;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class NbResponse {

  private static final Map<String, Object> OK_BODY = Map.of("result", true);
  private static final ResponseEntity<Map<String, Object>> OK = ResponseEntity.ok(OK_BODY);

  public static ResponseEntity<?> success() {
    return OK;
  }

  public static ResponseEntity<?> failure() {
    return ResponseEntity.ok(Map.of("result", false));
  }

  public static ResponseEntity<?> failure(String message) {
    return ResponseEntity.ok(Map.of("result", false, "message", message));
  }

  public static ResponseEntity<?> failure(String message, Throwable ex) {
    return ResponseEntity.ok(
        Map.of("result", false, "message", message, "exception", ex.getMessage()));
  }
}
