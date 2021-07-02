package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhuqigong.blogservice.model.TagStatistics;
import org.zhuqigong.blogservice.service.TagService;

import java.util.List;

@RestController
@RequestMapping("/blog")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public List<TagStatistics> getTags() {
        return tagService.countTags();
    }
}
