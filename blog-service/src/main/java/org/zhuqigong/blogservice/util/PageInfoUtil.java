package org.zhuqigong.blogservice.util;

public class PageInfoUtil {
  private PageInfoUtil() {
  }

  public static Integer adaptPage(Integer page) {
    //page should start with 0
    page = Math.max(page - 1, 0);
    //max request page is 100
    page = Math.min(page, 100);
    return page;
  }

  public static Integer adaptSize(Integer size) {
    //min 1items be request
    size = Math.max(size, 1);
    //max  10 items be request in single request
    size = Math.min(size, 10);
    return size;
  }

  public static Integer getTotalPage(Long total, Integer adaptSize) {
    return (int) ((total / adaptSize) + (total % adaptSize == 0 ? 0 : 1));
  }
}
