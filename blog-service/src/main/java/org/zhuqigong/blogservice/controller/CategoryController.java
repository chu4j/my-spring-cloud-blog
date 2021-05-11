package org.zhuqigong.blogservice.controller;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.CategoryCount;
import org.zhuqigong.blogservice.service.CategoryService;

@RestController
@RequestMapping("/blog")
public class CategoryController {
  private final CategoryService categoryService;

  public CategoryController(CategoryService categoryService) {
    this.categoryService = categoryService;
  }

  @GetMapping("/categories")
  public List<CategoryCount> getCategories() {
    return categoryService.findCategories()
        .stream()
        .collect(Collectors.groupingBy(Category::getCategory, HashMap::new, Collectors.counting()))
        .entrySet()
        .stream().map(entry -> new CategoryCount(entry.getKey(), entry.getValue()))
        .sorted((o1, o2) -> o2.getCount().compareTo(o1.getCount()))
        .collect(Collectors.toList());
  }
}
