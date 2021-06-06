package org.zhuqigong.blogservice.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.zhuqigong.blogservice.exception.CategoryNotFoundException;
import org.zhuqigong.blogservice.exception.NotFoundException;
import org.zhuqigong.blogservice.exception.PostNotFoundException;
import org.zhuqigong.blogservice.exception.TagNotFoundException;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.PagePost;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.repository.CategoryRepository;
import org.zhuqigong.blogservice.repository.PostRepository;
import org.zhuqigong.blogservice.repository.TagRepository;
import org.zhuqigong.blogservice.util.CacheUtil;
import org.zhuqigong.blogservice.util.MarkdownUtil;
import org.zhuqigong.blogservice.util.PageInfoUtil;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@SuppressWarnings("unchecked")
public class PostService {
    private static final String SORT_PROPERTIES = "publishTime";
    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final Cache<String, Object> cache;
    private final HttpServletRequest request;
    @Value("${my.blog.markdown-file.dir}")
    private String markdownFileDir;

    public PostService(PostRepository postRepository, CategoryRepository categoryRepository,
                       TagRepository tagRepository, Cache<String, Object> cache,
                       HttpServletRequest request) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.cache = cache;
        this.request = request;
    }

    @Cacheable("posts")
    public PagePost getPosts(int page, int size) {
        size = PageInfoUtil.adaptSize(size);
        page = PageInfoUtil.adaptPage(page);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(SORT_PROPERTIES).descending());
        long total = postRepository.count();
        int totalPage = PageInfoUtil.getTotalPage(total, size);
        LOG.info("Get Post : page:[{}],size:[{}]", page, size);
        String cacheKey = CacheUtil.generateFixedCacheKey(request, page, size);
        LOG.info("Cache Key :[{}]", cacheKey);
        Object cachePostData = cache.getIfPresent(cacheKey);
        if (cachePostData == null) {
            LOG.info("Cache Not Found");
            List<Post> data = postRepository.findAll(pageRequest).toList();
            cache.put(cacheKey, data);
            return new PagePost(total, totalPage, page + 1, data);
        } else {
            LOG.info("Return Cache Directly");
            return new PagePost(total, totalPage, page + 1, (List<Post>) cachePostData);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public Post createPost(Post post) {
        cache.cleanUp();
        cache.invalidateAll();
        Post newPost;
        if (null != post.getId()) {
            Post oldPost = postRepository.findById(post.getId()).orElse(new Post());
            String title = oldPost.getTitle();
            Map<String, Long> categoriesMap = oldPost.getCategories().stream().collect(Collectors.toMap(Category::getCategory, Category::getId));
            Map<String, Long> tagsMap = oldPost.getTags().stream().collect(Collectors.toMap(Tag::getTag, Tag::getId));
            newPost = MarkdownUtil.format(post.getTitle(), post.getContent());
            newPost.setId(post.getId());
            newPost.setCategories(newPost.getCategories().stream().map(c -> new Category(categoriesMap.getOrDefault(c.getCategory(), null), c.getCategory())).collect(Collectors.toList()));
            newPost.setTags(newPost.getTags().stream().map(t -> new Tag(tagsMap.getOrDefault(t.getTag(), null), t.getTag())).collect(Collectors.toList()));
            categoryRepository.saveAll(newPost.getCategories());
            tagRepository.saveAll(newPost.getTags());
            postRepository.save(newPost);
            String oldMarkdownFilePath = markdownFileDir + File.separator + title + ".md";
            try {
                Files.delete(Paths.get(oldMarkdownFilePath));
            } catch (IOException e) {
                LOG.debug("File:{} delete failed", oldMarkdownFilePath);
            }
        } else {
            newPost = MarkdownUtil.format(post.getTitle(), post.getContent());
            categoryRepository.saveAll(newPost.getCategories());
            tagRepository.saveAll(newPost.getTags());
            LOG.info("Create Post:Post title:{}", newPost.getTitle());
            postRepository.save(newPost);
        }
        //Save markdown text as file
        String newMarkdownFilePath = markdownFileDir + File.separator + newPost.getTitle() + ".md";
        try {
            FileUtils.writeStringToFile(new File(newMarkdownFilePath), newPost.getContent(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.info("Save File:[{}] failed", newMarkdownFilePath);
        }
        return newPost;
    }

    @Cacheable("postById")
    public Post findPostByPostId(Long postId) throws PostNotFoundException {
        String key = CacheUtil.generateFixedCacheKey(request, postId);
        Post cachePost = (Post) cache.getIfPresent(key);
        LOG.info("Get Post By Id : Post Id:[{}]", postId);
        if (cachePost == null) {
            Post post = postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post not found.Post id:" + postId));
            Post prevPost = postRepository.findFirstByPublishTimeAfterOrderByPublishTimeAsc(post.getPublishTime());
            Post nextPost = postRepository.findFirstByPublishTimeBeforeOrderByPublishTimeDesc(post.getPublishTime());
            if (null != prevPost) {
                post.setPrevPost(new Post.PostExtra(prevPost.getId(), prevPost.getTitle()));
            }
            if (null != nextPost) {
                post.setNextPost(new Post.PostExtra(nextPost.getId(), nextPost.getTitle()));
            }
            LOG.info("Cache Key Not Found,Cache Key:[{}]", key);
            cache.put(key, post);
            return post;
        } else {
            LOG.info("Cache Key Found,Cache Key[{}]", key);
            return cachePost;
        }
    }

    @Cacheable("postByTitle")
    public Post findPostByPostTitle(String postTitle) throws PostNotFoundException {
        LOG.info("Get Post By Title:Post Title:[{}]", postTitle);
        String key = CacheUtil.generateFixedCacheKey(request, postTitle);
        Post cachePost = (Post) cache.getIfPresent(key);
        if (cachePost == null) {
            Post post = postRepository.findPostByTitle(postTitle).orElseThrow(() -> new PostNotFoundException("Post not found.Post title : " + postTitle));
            cache.put(key, post);
            return post;
        } else {
            return cachePost;
        }
    }

    @Cacheable("postByCategoryName")
    @Transactional(propagation = Propagation.REQUIRED)
    public PagePost findPostByCategory(String categoryName, int page, int size)
            throws PostNotFoundException, CategoryNotFoundException {
        String key = CacheUtil.generateFixedCacheKey(request, categoryName, page, size);
        PagePost cachePosts = (PagePost) cache.getIfPresent(key);
        if (cachePosts == null) {
            List<Category> categoryList = categoryRepository.findByCategory(categoryName).orElseThrow(() -> new CategoryNotFoundException(String.format("Category:[%s] Not Found", categoryName)));
            page = PageInfoUtil.adaptPage(page);
            size = PageInfoUtil.adaptSize(size);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(SORT_PROPERTIES).descending());
            List<Post> list = postRepository.findPostByCategoriesIn(pageRequest, categoryList).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Category:[%s]", categoryName))).toList();
            long total = postRepository.countPostByCategoriesIn(categoryList);
            int totalPage = PageInfoUtil.getTotalPage(total, size);
            PagePost pagePost = new PagePost(total, totalPage, page, list);
            cache.put(key, pagePost);
            return pagePost;
        } else {
            return cachePosts;
        }
    }

    @Cacheable("postByTagName")
    public PagePost findPostByTag(String tagName, Integer page, Integer size) throws NotFoundException {
        String key = CacheUtil.generateFixedCacheKey(request, tagName, page, size);
        PagePost cachePosts = (PagePost) cache.getIfPresent(key);
        if (cachePosts == null) {
            List<Tag> tagList = tagRepository.findByTag(tagName).orElseThrow(() -> new TagNotFoundException(String.format("Can Not Found Post :Tag[%s]", tagName)));
            page = PageInfoUtil.adaptPage(page);
            size = PageInfoUtil.adaptSize(size);
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(SORT_PROPERTIES).descending());
            List<Post> list = postRepository.findPostByTagsIn(pageRequest, tagList).orElseThrow(() -> new PostNotFoundException(String.format("Can Not Found Post :Tag:[%s]", tagName))).toList();
            long total = postRepository.countPostByTagsIn(tagList);
            int totalPage = PageInfoUtil.getTotalPage(total, size);
            PagePost pagePost = new PagePost(total, totalPage, page, list);
            cache.put(key, pagePost);
            return pagePost;
        } else {
            return cachePosts;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ResponseEntity<String> deletePost(Long postId) {
        try {
            Post p = postRepository.findById(postId).orElse(null);
            //delete markdown file
            if (null != p) {
                String markdownFilePath = markdownFileDir + File.separator + p.getTitle() + ".md";
                Files.delete(Paths.get(markdownFilePath));
                postRepository.delete(p);
                cache.cleanUp();
                cache.invalidateAll();
                return ResponseEntity.ok("Post by delete success which id is " + postId);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post Not Found");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return ResponseEntity.status(500).body("Post delete failed : " + e.getMessage());
        }
    }

    public ResponseEntity<String> formatMarkdownText2Html(String markdownText) {
        try {
            return ResponseEntity.ok(MarkdownUtil.formatMarkdown2Html(markdownText, false));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Markdown text format to html failed : " + e.getMessage());
        }
    }
}
