package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.service.TagService;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blog")
public class TagController {
    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/tags")
    public List<Tag> getTags() {
        return tagService.findTags()
                .stream()
                .collect(Collectors.groupingBy(Tag::getTagName, HashMap::new, Collectors.counting()))
                .entrySet()
                .stream().map(entry -> new Tag(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> o2.getCount().compareTo(o1.getCount()))
                .collect(Collectors.toList());
    }
}
