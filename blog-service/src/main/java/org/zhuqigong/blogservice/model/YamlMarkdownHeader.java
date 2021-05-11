package org.zhuqigong.blogservice.model;

import java.util.Date;
import java.util.List;

public class YamlMarkdownHeader {
  private String title;
  private String author;
  private Date date;
  private List<String> categories;
  private List<String> tags;

  public YamlMarkdownHeader() {
  }

  public YamlMarkdownHeader(String title, String author, Date date,
                            List<String> categories, List<String> tags) {
    this.title = title;
    this.author = author;
    this.date = date;
    this.categories = categories;
    this.tags = tags;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public List<String> getCategories() {
    return categories;
  }

  public void setCategories(List<String> categories) {
    this.categories = categories;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }
}
