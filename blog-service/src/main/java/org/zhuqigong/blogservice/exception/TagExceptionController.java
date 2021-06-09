package org.zhuqigong.blogservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class TagExceptionController {
    @ExceptionHandler(value = TagNotFoundException.class)
    public ResponseEntity<Object> tagNotFoundException(TagNotFoundException tagNotFoundException) {
        return new ResponseEntity<>(tagNotFoundException.getMessage(), HttpStatus.NOT_FOUND);
    }
}
