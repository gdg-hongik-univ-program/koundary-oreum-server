package com.koundary.domain.user.exception;

public class DuplicateLoginIdException extends RuntimeException {
  public DuplicateLoginIdException(String message) {
    super(message);
  }
}
