package org.zhuqigong.blogservice.service;

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
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.ResponseBuilder;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.repository.CategoryRepository;
import org.zhuqigong.blogservice.repository.PostRepository;
import org.zhuqigong.blogservice.repository.TagRepository;
import org.zhuqigong.blogservice.util.CacheUtil;
import org.zhuqigong.blogservice.util.MarkdownUtil;
import org.zhuqigong.blogservice.util.PageParamsUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PostService {
    private static final String TOTAL = "total";
    private static final String TOTAL_PAGE = "totalPage";
    private static final String CURRENT_PAGE = "currentPage";
    private static final String LIST = "list";
    private static final String SORT_PROPERTIES = "publishTime";
    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CacheUtil cacheUtil;
    @Value("${my.blog.markdown-file.dir}")
    private String markdownFileDir;

    public PostService(PostRepository postRepository, CategoryRepository categoryRepository,
                       TagRepository tagRepository, CacheUtil cacheUtil) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.cacheUtil = cacheUtil;
    }

    public Map<String, Object> getPosts(int pageNumber, int size) {
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, size, Sort.by(SORT_PROPERTIES).descending());
        long total = postRepository.count();
        int totalPage = PageParamsUtil.calculateTotalPage(total, size);
        LOG.info("Get Post : page:[{}],size:[{}]", pageNumber, size);
        String cacheKey = cacheUtil.getCacheKey();
        LOG.info("Cache Key :[{}]", cacheKey);
        Collection<Post> cachePostList = cacheUtil.getCache(cacheKey);
        if (cachePostList.isEmpty()) {
            LOG.info("Cache Not Found");
            List<Post> data = postRepository.findAll(pageRequest).toList();
            cacheUtil.storeCache(cacheKey, data);
            return new ResponseBuilder().status(200).message("request success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(LIST, data)
                    .build();
        } else {
            LOG.info("Return Cache Directly");
            return new ResponseBuilder().status(200).message("request success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(LIST, cachePostList)
                    .build();
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
                    .status(200)
                    .message("Update post success")
                    .append("id", aPost.getId())
                    .build();
        } else {
            Post repeatTitlePost = postRepository.findPostByTitle(post.getTitle()).orElse(null);
            if (repeatTitlePost == null) {
                Post aPost = MarkdownUtil.format(post.getTitle(), post.getContent());
                categoryRepository.saveAll(aPost.getCategories());
                tagRepository.saveAll(aPost.getTags());
                LOG.info("Create Post:Post title:{}", aPost.getTitle());
                aPost.setRemark1(post.getRemark1());
                postRepository.save(aPost);
                //Save markdown text as file
                String markdownFilePath = markdownFileDir + File.separator + aPost.getTitle() + ".md";
                try {
                    FileUtils.writeStringToFile(new File(markdownFilePath), aPost.getContent(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    LOG.info("Save File:[{}] failed", markdownFilePath);
                }
                return new ResponseBuilder()
                        .status(200)
                        .message("Insert new post success")
                        .append("id", aPost.getId())
                        .build();
            } else {
                LOG.info("Repeat title is exists , repeat title is [{}]", repeatTitlePost.getTitle());
                return new ResponseBuilder()
                        .status(200)
                        .message("Insert new post failed,because repeat title exists")
                        .append("id", repeatTitlePost.getId())
                        .build();
            }
        }

    }

    public Post findPostByPostId(Long postId) throws PostNotFoundException {
        String key = cacheUtil.getCacheKey();
        Collection<Post> cachePost = cacheUtil.getCache(key);
        LOG.info("Get Post By Id : Post Id:[{}]", postId);
        if (cachePost.isEmpty()) {
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
            cacheUtil.storeCache(key, Collections.singleton(post));
            return post;
        } else {
            LOG.info("Cache Key Found,Cache Key[{}]", key);
            return cachePost.stream().findFirst().orElse(new Post());
        }
    }

    public Post findPostByPostTitle(String postTitle) throws PostNotFoundException {
        LOG.info("Get Post By Title:Post Title:[{}]", postTitle);
        String key = cacheUtil.getCacheKey();
        Collection<Post> cachePost = cacheUtil.getCache(key);
        if (cachePost.isEmpty()) {
            Post post = postRepository.findPostByTitle(postTitle).orElseThrow(() -> new PostNotFoundException("Post not found.Post title : " + postTitle));
            cacheUtil.storeCache(key, Collections.singleton(post));
            return post;
        } else {
            return cachePost.stream().findFirst().orElse(new Post());
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> findPostByCategory(String categoryName, int pageNumber, int size)
            throws PostNotFoundException, CategoryNotFoundException {
        String cacheKey = cacheUtil.getCacheKey();
        Collection<Post> cachePosts = cacheUtil.getCache(cacheKey);
        List<Category> categoryList = categoryRepository.findByCategoryName(categoryName).orElseThrow(() -> new CategoryNotFoundException(String.format("Category:[%s] Not Found", categoryName)));
        if (cachePosts.isEmpty()) {
            PageRequest pageRequest = PageRequest.of(pageNumber - 1, size, Sort.by(SORT_PROPERTIES).descending());
            List<Post> list = postRepository.findPostByCategoriesIn(pageRequest, categoryList).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Category:[%s]", categoryName))).toList();
            long total = postRepository.countPostByCategoriesIn(categoryList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            cacheUtil.storeCache(cacheKey, list);
            return new ResponseBuilder().status(200).message("request post by category success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(LIST, list)
                    .build();
        } else {
            long total = postRepository.countPostByCategoriesIn(categoryList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            return new ResponseBuilder().status(200).message("request post by category success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(LIST, cachePosts)
                    .build();
        }
    }

    public Map<String, Object> findPostByTag(String tagName, Integer pageNumber, Integer size) throws NotFoundException {
        String key = cacheUtil.getCacheKey();
        Collection<Post> cachePosts = cacheUtil.getCache(key);
        List<Tag> tagList = tagRepository.findByTagName(tagName).orElseThrow(() -> new TagNotFoundException(String.format("Can Not Found Post :Tag[%s]", tagName)));
        if (cachePosts.isEmpty()) {
            PageRequest pageRequest = PageRequest.of(pageNumber - 1, size, Sort.by(SORT_PROPERTIES).descending());
            List<Post> list = postRepository.findPostByTagsIn(pageRequest, tagList).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Tag:[%s]", tagName))).toList();
            long total = postRepository.countPostByTagsIn(tagList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            cacheUtil.storeCache(key, list);
            return new ResponseBuilder().status(200).message("get post by tag success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(LIST, list)
                    .build();
        } else {
            long total = postRepository.countPostByTagsIn(tagList);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            return new ResponseBuilder().status(200).message("get post by tag success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(LIST, cachePosts)
                    .build();
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
                return new ResponseBuilder().message("Post by delete success that id is " + postId).build();
            } else {
                return new ResponseBuilder().message("Post Not Found").build();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return new ResponseBuilder().message("Post delete failed : " + e.getMessage()).build();
        }
    }
}
