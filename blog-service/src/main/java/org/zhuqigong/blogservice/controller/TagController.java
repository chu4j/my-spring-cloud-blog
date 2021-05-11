package org.zhuqigong.blogservice.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.CategoryCount;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.model.TagCount;
import org.zhuqigong.blogservice.service.TagService;

@RestController
@RequestMapping("/blog")
public class TagController {
  private final TagService tagService;

  public TagController(TagService tagService) {
    this.tagService = tagService;
  }

  @GetMapping("/tags")
  public List<TagCount> getTags() {
    return tagService.findTags()
        .stream()
        .collect(Collectors.groupingBy(Tag::getTag, HashMap::new, Collectors.counting()))
        .entrySet()
        .stream().map(entry->new TagCount(entry.getKey(),entry.getValue()))
        .sorted((o1, o2) -> o2.getCount().compareTo(o1.getCount()))
        .collect(Collectors.toList());
  }
}
