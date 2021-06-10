package org.zhuqigong.blogservice.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zhuqigong.blogservice.anotations.CleanUpCache;
import org.zhuqigong.blogservice.exception.CategoryNotFoundException;
import org.zhuqigong.blogservice.exception.NotFoundException;
import org.zhuqigong.blogservice.exception.PostNotFoundException;
import org.zhuqigong.blogservice.exception.TagNotFoundException;
import org.zhuqigong.blogservice.model.*;
import org.zhuqigong.blogservice.repository.CategoryRepository;
import org.zhuqigong.blogservice.repository.PostRepository;
import org.zhuqigong.blogservice.repository.TagRepository;
import org.zhuqigong.blogservice.util.CacheUtil;
import org.zhuqigong.blogservice.util.MarkdownUtil;
import org.zhuqigong.blogservice.util.PageParamsUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("unchecked")
public class PostService {
    private static final String SORT_PROPERTIES = "publishTime";
    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final Cache<String, Collection<Post>> cache;
    private final HttpServletRequest request;
    @Value("${my.blog.markdown-file.dir}")
    private String markdownFileDir;

    public PostService(PostRepository postRepository, CategoryRepository categoryRepository,
                       TagRepository tagRepository, Cache<String, Collection<Post>> cache, HttpServletRequest request) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.cache = cache;
        this.request = request;
    }

    public PostResponseEntity getPosts(int pageNumber, int size) {
        size = PageParamsUtil.securePageSize(size);
        pageNumber = PageParamsUtil.securePageNumber(pageNumber);
        PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by(SORT_PROPERTIES).descending());
        long total = postRepository.count();
        int totalPage = PageParamsUtil.calculateTotalPage(total, size);
        LOG.info("Get Post : page:[{}],size:[{}]", pageNumber, size);
        String cacheKey = CacheUtil.getCacheKey(request, pageNumber, size);
        LOG.info("Cache Key :[{}]", cacheKey);
        Object cachePostData = cache.getIfPresent(cacheKey);
        if (cachePostData == null) {
            LOG.info("Cache Not Found");
            List<Post> data = postRepository.findAll(pageRequest).toList();
            cache.put(cacheKey, data);
            return new PostResponseEntity(total, totalPage, pageNumber + 1, data);
        } else {
            LOG.info("Return Cache Directly");
            return new PostResponseEntity(total, totalPage, pageNumber + 1, (List<Post>) cachePostData);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @CleanUpCache
    public Map<String, Object> saveOrUpdatePost(Post post) {
        if (null != post.getId()) {
            Post oldPost = postRepository.findById(post.getId()).orElse(new Post());
            String title = oldPost.getTitle();
            Post aPost = MarkdownUtil.format(post.getTitle(), post.getContent());
            aPost.setId(oldPost.getId());
            categoryRepository.deleteAll(oldPost.getCategories());
            tagRepository.deleteAll(oldPost.getTags());
            categoryRepository.saveAll(aPost.getCategories());
            tagRepository.saveAll(aPost.getTags());
            postRepository.save(aPost);
            String oldMarkdownFilePath = markdownFileDir + File.separator + title + ".md";
            String newMarkdownFilePath = markdownFileDir + File.separator + aPost.getTitle() + ".md";
            try {
                Files.delete(Paths.get(oldMarkdownFilePath));
                FileUtils.writeStringToFile(new File(newMarkdownFilePath), aPost.getContent(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOG.info("File:{} delete failed", oldMarkdownFilePath);
            }
            return new ResponseBuilder()
                    .append("status", 200)
                    .append("message", "Update post success")
                    .append("id", aPost.getId())
                    .build();
        } else {
            Post repeatTitlePost = postRepository.findPostByTitle(post.getTitle()).orElse(null);
            if (repeatTitlePost == null) {
                Post aPost = MarkdownUtil.format(post.getTitle(), post.getContent());
                categoryRepository.saveAll(aPost.getCategories());
                tagRepository.saveAll(aPost.getTags());
                LOG.info("Create Post:Post title:{}", aPost.getTitle());
                postRepository.save(aPost);
                //Save markdown text as file
                String markdownFilePath = markdownFileDir + File.separator + aPost.getTitle() + ".md";
                try {
                    FileUtils.writeStringToFile(new File(markdownFilePath), aPost.getContent(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    LOG.info("Save File:[{}] failed", markdownFilePath);
                }
                return new ResponseBuilder()
                        .append("status", 200)
                        .append("message", "Insert new post success")
                        .append("id", aPost.getId())
                        .build();
            } else {
                LOG.info("Repeat title is exists , repeat title is [{}]", repeatTitlePost.getTitle());
                return new ResponseBuilder()
                        .append("status", 200)
                        .append("message", "Insert new post failed,because repeat title exists")
                        .append("id", repeatTitlePost.getId())
                        .build();
            }
        }

    }

    public Post findPostByPostId(Long postId) throws PostNotFoundException {
        String key = CacheUtil.getCacheKey(request, postId);
        Post cachePost = cache.asMap().getOrDefault(key, Collections.emptyList()).stream().findFirst().orElse(null);
        LOG.info("Get Post By Id : Post Id:[{}]", postId);
        if (cachePost == null) {
            Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found.Post id:" + postId));
            Post prevPost = postRepository.findFirstByPublishTimeAfterOrderByPublishTimeAsc(post.getPublishTime());
            Post nextPost = postRepository.findFirstByPublishTimeBeforeOrderByPublishTimeDesc(post.getPublishTime());
            if (null != prevPost) {
                post.setPrevPost(new Post.NexusPost(prevPost.getId(), prevPost.getTitle()));
            }
            if (null != nextPost) {
                post.setNextPost(new Post.NexusPost(nextPost.getId(), nextPost.getTitle()));
            }
            LOG.info("Cache Key Not Found,Cache Key:[{}]", key);
            cache.put(key, Collections.singleton(post));
            return post;
        } else {
            LOG.info("Cache Key Found,Cache Key[{}]", key);
            return cachePost;
        }
    }

    public Post findPostByPostTitle(String postTitle) throws PostNotFoundException {
        LOG.info("Get Post By Title:Post Title:[{}]", postTitle);
        String key = CacheUtil.getCacheKey(request, postTitle);
        Post cachePost = cache.asMap().getOrDefault(key, Collections.emptyList()).stream().findFirst().orElse(null);
        if (cachePost == null) {
            Post post = postRepository.findPostByTitle(postTitle).orElseThrow(() -> new PostNotFoundException("Post not found.Post title : " + postTitle));
            cache.put(key, Collections.singleton(post));
            return post;
        } else {
            return cachePost;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public PostResponseEntity findPostByCategory(String categoryName, int pageNumber, int size)
            throws PostNotFoundException, CategoryNotFoundException {
        String cacheKey = CacheUtil.getCacheKey(request, categoryName, pageNumber, size);
        Collection<Post> cachePosts = cache.asMap().getOrDefault(cacheKey, Collections.emptyList());
        List<Category> categoryList = categoryRepository.findByCategory(categoryName).orElseThrow(() -> new CategoryNotFoundException(String.format("Category:[%s] Not Found", categoryName)));
        if (cachePosts.isEmpty()) {
            pageNumber = PageParamsUtil.securePageNumber(pageNumber);
            size = PageParamsUtil.securePageSize(size);
            PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by(SORT_PROPERTIES).descending());
            List<Post> list = postRepository.findPostByCategoriesIn(pageRequest, categoryList).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Category:[%s]", categoryName))).toList();
            long total = postRepository.countPostByCategoriesIn(categoryList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            cache.put(cacheKey, list);
            return new PostResponseEntity(total, totalPage, pageNumber, list);
        } else {
            long total = postRepository.countPostByCategoriesIn(categoryList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            return new PostResponseEntity(total, totalPage, pageNumber, new ArrayList<>(cachePosts));
        }
    }

    public PostResponseEntity findPostByTag(String tagName, Integer pageNumber, Integer size) throws NotFoundException {
        String key = CacheUtil.getCacheKey(request, tagName, pageNumber, size);
        Collection<Post> cachePosts = cache.asMap().getOrDefault(key, Collections.emptyList());
        List<Tag> tagList = tagRepository.findByTag(tagName).orElseThrow(() -> new TagNotFoundException(String.format("Can Not Found Post :Tag[%s]", tagName)));
        if (cachePosts.isEmpty()) {
            pageNumber = PageParamsUtil.securePageNumber(pageNumber);
            size = PageParamsUtil.securePageSize(size);
            PageRequest pageRequest = PageRequest.of(pageNumber, size, Sort.by(SORT_PROPERTIES).descending());
            List<Post> list = postRepository.findPostByTagsIn(pageRequest, tagList).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Tag:[%s]", tagName))).toList();
            long total = postRepository.countPostByTagsIn(tagList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            cache.put(key, list);
            return new PostResponseEntity(total, totalPage, pageNumber, list);
        } else {
            long total = postRepository.countPostByTagsIn(tagList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            return new PostResponseEntity(total, totalPage, pageNumber, new ArrayList<>(cachePosts));
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @CleanUpCache
    public Map<String, Object> deletePost(Long postId) {
        try {
            Post p = postRepository.findById(postId).orElse(new Post());
            postRepository.delete(p);
            //delete markdown file
            if (null != p.getId()) {
                String markdownFilePath = markdownFileDir + File.separator + p.getTitle() + ".md";
                Files.delete(Paths.get(markdownFilePath));
                return new ResponseBuilder().append("message", "Post by delete success that id is " + postId).build();
            } else {
                return new ResponseBuilder().append("message", "Post Not Found").build();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return new ResponseBuilder().append("message", "Post delete failed : " + e.getMessage()).build();
        }
    }
}
