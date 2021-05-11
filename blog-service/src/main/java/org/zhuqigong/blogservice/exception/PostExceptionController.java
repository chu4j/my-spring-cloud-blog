package org.zhuqigong.blogservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class PostExceptionController {
  @ExceptionHandler(value = JsonFormatPostException.class)
  public ResponseEntity<Object> jsonFormatPostException(JsonFormatPostException exception) {
    return new ResponseEntity<>(exception.getMessage(),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = PostNotFoundException.class)
  public ResponseEntity<Object> postNotFoundException(PostNotFoundException exception) {
    return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
  }
}
