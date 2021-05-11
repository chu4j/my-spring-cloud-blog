package org.zhuqigong.blogservice.exception;

public class PostNotFoundException extends NotFoundException {
  public PostNotFoundException(String message) {
    super(message);
  }
}
