package org.zhuqigong.blogservice.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zhuqigong.blogservice.anotations.CleanUpCache;
import org.zhuqigong.blogservice.exception.NotFoundException;
import org.zhuqigong.blogservice.exception.PostNotFoundException;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.ResponseEntityBuilder;
import org.zhuqigong.blogservice.repository.CategoryRepository;
import org.zhuqigong.blogservice.repository.PostRepository;
import org.zhuqigong.blogservice.repository.TagRepository;
import org.zhuqigong.blogservice.util.MarkdownUtil;
import org.zhuqigong.blogservice.util.PageParamsUtil;
import org.zhuqigong.blogservice.util.PostCacheOps;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PostService {
    private static final String TOTAL = "total";
    private static final String TOTAL_PAGE = "totalPage";
    private static final String CURRENT_PAGE = "currentPage";
    private static final String DATA = "list";
    private static final String SORT_PROPERTIES = "publishTime";
    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postDao;
    private final CategoryRepository categoryDao;
    private final TagRepository tagDao;
    private final PostCacheOps postCacheOps;
    @Value("${my.blog.markdown-file.dir}")
    private String markdownFileDirectory;

    public PostService(PostRepository postDao, CategoryRepository categoryDao,
                       TagRepository tagDao, PostCacheOps postCacheOps) {
        this.postDao = postDao;
        this.categoryDao = categoryDao;
        this.tagDao = tagDao;
        this.postCacheOps = postCacheOps;
    }

    public Map<String, Object> findPosts(int pageNumber, int size) {
        PageRequest pageRequest = PageRequest.of(pageNumber - 1, size, Sort.by(SORT_PROPERTIES).descending());
        long total = postDao.count();
        int totalPage = PageParamsUtil.calculateTotalPage(total, size);
        LOGGER.info("Get Post : page:[{}],size:[{}]", pageNumber, size);
        String cacheKey = postCacheOps.generateKeyByRequestUrl();
        LOGGER.info("Cache Key :[{}]", cacheKey);
        List<Post> cachePostList = postCacheOps.getElements(cacheKey);
        if (cachePostList.isEmpty()) {
            LOGGER.info("Cache Not Found");
            List<Post> data = postDao.findAll(pageRequest).toList();
            postCacheOps.saveOrUpdateElements(cacheKey, data);
            return new ResponseEntityBuilder().statusCode(200).message("request success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(DATA, data)
                    .build();
        } else {
            LOGGER.info("Return Cache Directly");
            return new ResponseEntityBuilder().statusCode(200).message("request success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(DATA, cachePostList)
                    .build();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @CleanUpCache({CleanUpCache.TYPE.ELEMENT, CleanUpCache.TYPE.ELEMENTS})
    public Map<String, Object> createOrUpdatePost(Long id, String title, String content) {
        if (id != null) {
            Post oldPost = postDao.findById(id).orElse(new Post());
            Post post = MarkdownUtil.format(title, content);
            post.setId(oldPost.getId());
            categoryDao.deleteAll(oldPost.getCategories());
            tagDao.deleteAll(oldPost.getTags());
            categoryDao.saveAll(post.getCategories());
            tagDao.saveAll(post.getTags());
            postDao.save(post);
            String oldMarkdownFilePath = markdownFileDirectory + File.separator + title + ".md";
            String newMarkdownFilePath = markdownFileDirectory + File.separator + post.getTitle() + ".md";
            try {
                Files.delete(Paths.get(oldMarkdownFilePath));
                FileUtils.writeStringToFile(new File(newMarkdownFilePath), post.getContent(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.info("File:{} delete failed", oldMarkdownFilePath);
            }
            return new ResponseEntityBuilder()
                    .statusCode(200)
                    .message("Update post success")
                    .append("id", post.getId())
                    .build();
        } else {
            Post repeatTitlePost = postDao.findPostByTitle(title).orElse(null);
            if (repeatTitlePost == null) {
                Post post = MarkdownUtil.format(title, content);
                categoryDao.saveAll(post.getCategories());
                tagDao.saveAll(post.getTags());
                LOGGER.info("Create Post:Post title:{}", post.getTitle());
                postDao.save(post);
                //Save markdown text as file
                String markdownFilePath = markdownFileDirectory + File.separator + post.getTitle() + ".md";
                try {
                    FileUtils.writeStringToFile(new File(markdownFilePath), post.getContent(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    LOGGER.info("Save File:[{}] failed", markdownFilePath);
                }
                return new ResponseEntityBuilder()
                        .statusCode(200)
                        .message("Insert new post success")
                        .append("id", post.getId())
                        .build();
            } else {
                LOGGER.info("Repeat title is exists , repeat title is [{}]", repeatTitlePost.getTitle());
                return new ResponseEntityBuilder()
                        .statusCode(200)
                        .message("Insert new post failed,because repeat title exists")
                        .append("id", repeatTitlePost.getId())
                        .build();
            }
        }

    }

    public Post findPostById(Long id) throws PostNotFoundException {
        String cacheKey = postCacheOps.generateKeyByRequestUrl();
        Post cachePost = postCacheOps.getElement(cacheKey);
        LOGGER.info("Get Post By Id : Post Id:[{}]", id);
        if (cachePost == null) {
            Post post = postDao.findById(id).orElseThrow(() -> new PostNotFoundException("Post not found.Post id:" + id));
            Post prevPost = postDao.findFirstByPublishTimeAfterOrderByPublishTimeAsc(post.getPublishTime());
            Post nextPost = postDao.findFirstByPublishTimeBeforeOrderByPublishTimeDesc(post.getPublishTime());
            if (null != prevPost) {
                post.setPrevPost(new Post.LatelyPost(prevPost.getId(), prevPost.getTitle()));
            }
            if (null != nextPost) {
                post.setNextPost(new Post.LatelyPost(nextPost.getId(), nextPost.getTitle()));
            }
            LOGGER.info("Cache Key Not Found,Cache Key:[{}]", cacheKey);
            postCacheOps.saveOrUpdateElement(cacheKey, post);
            return post;
        } else {
            LOGGER.info("Cache Key Found,Cache Key[{}]", cacheKey);
            return cachePost;
        }
    }

    public Post findPostByTitleName(String titleName) throws PostNotFoundException {
        LOGGER.info("Get Post By Title:Post Title:[{}]", titleName);
        String cacheKey = postCacheOps.generateKeyByRequestUrl();
        Post cachePost = postCacheOps.getElement(cacheKey);
        if (cachePost == null) {
            Post post = postDao.findPostByTitle(titleName).orElseThrow(() -> new PostNotFoundException("Post not found.Post title : " + titleName));
            postCacheOps.saveOrUpdateElement(cacheKey, post);
            return post;
        } else {
            return cachePost;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object> findPostByCategoryName(String categoryName, int pageNumber, int size)
            throws PostNotFoundException {
        String cacheKey = postCacheOps.generateKeyByRequestUrl();
        List<Post> cachePosts = postCacheOps.getElements(cacheKey);
        if (cachePosts.isEmpty()) {
            PageRequest pageRequest = PageRequest.of(pageNumber - 1, size);
            Page<Post> pagePost = postDao.findPostByCategoriesIn(categoryName, pageRequest).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Category:[%s]", categoryName)));
            long total = pagePost.getTotalElements();
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            postCacheOps.saveOrUpdateElements(cacheKey, pagePost.getContent());
            return new ResponseEntityBuilder().statusCode(200).message("request post by category success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(DATA, pagePost.getContent())
                    .build();
        } else {
            long total = postDao.countPostByCategoriesIn(categoryName);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            return new ResponseEntityBuilder().statusCode(200).message("request post by category success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(DATA, cachePosts)
                    .build();
        }
    }

    public Map<String, Object> findPostByTagName(String tagName, Integer pageNumber, Integer size) throws NotFoundException {
        String cacheKey = postCacheOps.generateKeyByRequestUrl();
        List<Post> cachePosts = postCacheOps.getElements(cacheKey);
        if (cachePosts.isEmpty()) {
            PageRequest pageRequest = PageRequest.of(pageNumber - 1, size);
            Page<Post> pagePost = postDao.findPostByTagsIn(tagName, pageRequest).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Tag:[%s]", tagName)));
            long total = pagePost.getTotalElements();
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            postCacheOps.saveOrUpdateElements(cacheKey, pagePost.getContent());
            return new ResponseEntityBuilder().statusCode(200).message("get post by tag success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(DATA, pagePost.getContent())
                    .build();
        } else {
            long total = postDao.countPostByTagsIn(tagName);
            int totalPage = PageParamsUtil.calculateTotalPage(total, size);
            return new ResponseEntityBuilder().statusCode(200).message("get post by tag success")
                    .append(TOTAL, total)
                    .append(TOTAL_PAGE, totalPage)
                    .append(CURRENT_PAGE, pageNumber)
                    .append(DATA, cachePosts)
                    .build();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @CleanUpCache(CleanUpCache.TYPE.ELEMENT)
    public Map<String, Object> deletePostById(Long postId) {
        try {
            Post p = postDao.findById(postId).orElse(new Post());
            postDao.delete(p);
            //delete markdown file
            if (null != p.getId()) {
                String markdownFilePath = markdownFileDirectory + File.separator + p.getTitle() + ".md";
                Files.delete(Paths.get(markdownFilePath));
                return new ResponseEntityBuilder().message("Post by delete success that id is " + postId).build();
            } else {
                return new ResponseEntityBuilder().message("Post Not Found").build();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return new ResponseEntityBuilder().message("Post delete failed : " + e.getMessage()).build();
        }
    }
}
