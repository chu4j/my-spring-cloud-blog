package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.service.CategoryService;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blog")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<Category> getCategories() {
        return categoryService.findCategories()
                .stream()
                .collect(Collectors.groupingBy(Category::getCategoryName, HashMap::new, Collectors.counting()))
                .entrySet()
                .stream().map(entry -> new Category(entry.getKey(), entry.getValue()))
                .sorted((o1, o2) -> o2.getCount().compareTo(o1.getCount()))
                .collect(Collectors.toList());
    }
}
