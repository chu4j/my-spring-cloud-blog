package org.zhuqigong.blogservice.model;

import java.util.List;

public class PagePost {
    private Long total;
    private Integer totalPage;
    private Integer currentPage;
    private List<Post> list;

    public PagePost() {
    }

    public PagePost(Long total, Integer totalPage, Integer currentPage,
                    List<Post> list) {
        this.total = total;
        this.totalPage = totalPage;
        this.currentPage = currentPage;
        this.list = list;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public List<Post> getList() {
        return list;
    }

    public void setList(List<Post> list) {
        this.list = list;
    }
}
