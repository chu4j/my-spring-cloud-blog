package org.zhuqigong.blogservice.util;

public class PageParamsUtil {
    private PageParamsUtil() {
    }

    public static int securePageNumber(int page) {
        //page should start with 0
        page = Math.max(page - 1, 0);
        //max request page is 300
        page = Math.min(page, 300);
        return page;
    }

    public static int securePageSize(int size) {
        //min 1items be request
        size = Math.max(size, 1);
        //max  10 items be request in single request
        size = Math.min(size, 10);
        return size;
    }

    public static int calculateTotalPage(long total, int adaptSize) {
        return (int) ((total / adaptSize) + (total % adaptSize == 0 ? 0 : 1));
    }
}
