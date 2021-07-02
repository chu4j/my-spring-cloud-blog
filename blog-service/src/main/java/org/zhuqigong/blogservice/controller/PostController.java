package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.*;
import org.zhuqigong.blogservice.exception.NotFoundException;
import org.zhuqigong.blogservice.exception.PostNotFoundException;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.service.PostService;

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
        return postService.findAllPost(page, size);
    }

    @GetMapping("/post/id/{postId}")
    public Post getPostByPostId(@PathVariable Long postId) throws PostNotFoundException {
        return postService.findPostById(postId);
    }

    @GetMapping("/post/title/{postTitle}")
    public Post getPostByPostTitle(@PathVariable String postTitle) throws PostNotFoundException {
        return postService.findPostByTitleName(postTitle);
    }

    @GetMapping("/category/{categoryName}")
    public Map<String, Object> getPostByCategoryName(@PathVariable String categoryName,
                                                     @RequestParam Integer page, @RequestParam Integer size)
            throws NotFoundException {
        return postService.findPostByCategoryName(categoryName, page, size);
    }

    @GetMapping("/tag/{tagName}")
    public Map<String, Object> getPostByTagName(@PathVariable String tagName, Integer page, Integer size)
            throws NotFoundException {
        return postService.findPostByTagName(tagName, page, size);
    }

    @GetMapping("/tiny/posts")
    public Map<String, Object> getTinyPosts(@RequestParam Integer page, @RequestParam Integer size) {
        return postService.findAllMinPost(page, size);
    }
}
