package org.zhuqigong.blogservice.model;

public class TagCount {
  private String tag;
  private Long count;

  public TagCount(String tag, Long count) {
    this.tag = tag;
    this.count = count;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }
}
