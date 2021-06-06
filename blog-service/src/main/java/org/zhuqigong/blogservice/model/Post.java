package org.zhuqigong.blogservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String title;
    private String author;
    @Lob
    private String content;
    @Lob
    private String contentBody;
    @Lob
    private String catalogue;
    @Lob
    private String catalogueBody;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "post_category")
    private List<Category> categories;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "post_tag")
    private List<Tag> tags;
    @Column(name = "publish_time")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date publishTime;
    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date createdTime;
    @UpdateTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private Date lastUpdatedTime;
    private String remark1;
    private String remark2;
    private String remark3;
    @Transient
    private PostExtra prevPost;
    @Transient
    private PostExtra nextPost;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCatalogue() {
        return catalogue;
    }

    public void setCatalogue(String catalogue) {
        this.catalogue = catalogue;
    }

    public String getCatalogueBody() {
        return catalogueBody;
    }

    public void setCatalogueBody(String catalogueBody) {
        this.catalogueBody = catalogueBody;
    }

    public String getRemark1() {
        return remark1;
    }

    public void setRemark1(String remake1) {
        this.remark1 = remake1;
    }

    public String getRemark2() {
        return remark2;
    }

    public void setRemark2(String remake2) {
        this.remark2 = remake2;
    }

    public String getRemark3() {
        return remark3;
    }

    public void setRemark3(String remake3) {
        this.remark3 = remake3;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentBody() {
        return contentBody;
    }

    public void setContentBody(String body) {
        this.contentBody = body;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public PostExtra getPrevPost() {
        return prevPost;
    }

    public void setPrevPost(PostExtra prevPost) {
        this.prevPost = prevPost;
    }

    public PostExtra getNextPost() {
        return nextPost;
    }

    public void setNextPost(PostExtra nextPost) {
        this.nextPost = nextPost;
    }

    public static class PostExtra {
        private Long id;
        private String title;

        public PostExtra() {
        }

        public PostExtra(Long id, String title) {
            this.id = id;
            this.title = title;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}


