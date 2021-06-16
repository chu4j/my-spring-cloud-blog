package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.*;
import org.zhuqigong.blogservice.exception.NotFoundException;
import org.zhuqigong.blogservice.exception.PostNotFoundException;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.service.PostService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/blog")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    public Map<String, Object> findPosts(@RequestParam int page, @RequestParam int size) {
        return postService.getPosts(page, size);
    }

    @GetMapping("/post/id/{postId}")
    public Post getPostByPostId(@PathVariable Long postId) throws PostNotFoundException {
        return postService.findPostByPostId(postId);
    }

    @GetMapping("/post/title/{postTitle}")
    public Post getPostByPostTitle(@PathVariable String postTitle) throws PostNotFoundException {
        return postService.findPostByPostTitle(postTitle);
    }

    @GetMapping("/category/{categoryName}")
    public Map<String, Object> getPostByCategoryName(@PathVariable String categoryName,
                                                     @RequestParam Integer page, @RequestParam Integer size)
            throws NotFoundException {
        return postService.findPostByCategory(categoryName, page, size);
    }

    @GetMapping("/tag/{tagName}")
    public Map<String, Object> getPostByTagName(@PathVariable String tagName, Integer page, Integer size)
            throws NotFoundException {
        return postService.findPostByTag(tagName, page, size);
    }

    @GetMapping("/tiny/posts")
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTinyPosts(@RequestParam Integer page, @RequestParam Integer size) {
        Map<String, Object> data = postService.getPosts(page, size);
        ((Collection<Post>) data.get("list"))
                .forEach(e -> {
                    e.setContentBody(null);
                    e.setContent(null);
                    e.setTags(null);
                    e.setCategories(null);
                    e.setAuthor(null);
                    e.setCatalogue(null);
                    e.setCatalogueBody(null);
                    e.setLastUpdatedTime(null);
                    e.setCreatedTime(null);
                });
        return data;
    }
}
