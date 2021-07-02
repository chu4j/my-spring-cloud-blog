package org.zhuqigong.blogservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public interface MinPost {
    Long getId();

    String getTitle();

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date getPublishTime();
}
