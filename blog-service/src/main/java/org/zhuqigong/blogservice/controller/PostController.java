package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.*;
import org.zhuqigong.blogservice.exception.NotFoundException;
import org.zhuqigong.blogservice.exception.PostNotFoundException;
import org.zhuqigong.blogservice.model.PostResponseEntity;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.service.PostService;

@RestController
@RequestMapping("/blog")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    public PostResponseEntity findPosts(@RequestParam int page, @RequestParam int size) {
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
    public PostResponseEntity getPostByCategoryName(@PathVariable String categoryName,
                                                    @RequestParam Integer page, @RequestParam Integer size)
            throws NotFoundException {
        return postService.findPostByCategory(categoryName, page, size);
    }

    @GetMapping("/tag/{tagName}")
    public PostResponseEntity getPostByTagName(@PathVariable String tagName, Integer page, Integer size)
            throws NotFoundException {
        return postService.findPostByTag(tagName, page, size);
    }

    @GetMapping("/tiny/posts")
    public PostResponseEntity getTinyPosts(@RequestParam Integer page, @RequestParam Integer size) {
        PostResponseEntity postResponseEntity = postService.getPosts(page, size);
        postResponseEntity.getList()
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
        return postResponseEntity;
    }
}
