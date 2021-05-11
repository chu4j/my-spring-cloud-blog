package org.zhuqigong.blogservice.exception;

public class TagNotFoundException extends NotFoundException{
  public TagNotFoundException(String message){
    super(message);
  }
}
