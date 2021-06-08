package org.zhuqigong.blogservice.model;

public class RestResponseEntity {
    private int status;
    private String message;

    public RestResponseEntity(int code, String message) {
        this.status = code;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
