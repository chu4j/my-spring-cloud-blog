package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.*;
import org.zhuqigong.blogservice.exception.NotFoundException;
import org.zhuqigong.blogservice.exception.PostNotFoundException;
import org.zhuqigong.blogservice.model.PagePost;
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
    public PagePost findPosts(@RequestParam int page, @RequestParam int size) {
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
    public PagePost getPostByCategoryName(@PathVariable String categoryName,
                                          @RequestParam Integer page, @RequestParam Integer size)
            throws NotFoundException {
        return postService.findPostByCategory(categoryName, page, size);
    }

    @GetMapping("/tag/{tagName}")
    public PagePost getPostByTagName(@PathVariable String tagName, Integer page, Integer size)
            throws NotFoundException {
        return postService.findPostByTag(tagName, page, size);
    }

    @GetMapping("/tiny/posts")
    public PagePost getTinyPosts(@RequestParam Integer page, @RequestParam Integer size) {
        PagePost pagePost = postService.getPosts(page, size);
        pagePost.getList()
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
        return pagePost;
    }
}
