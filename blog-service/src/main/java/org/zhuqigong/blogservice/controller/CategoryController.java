package org.zhuqigong.blogservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhuqigong.blogservice.model.CategoryStatistics;
import org.zhuqigong.blogservice.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/blog")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public List<CategoryStatistics> getCategories() {
        return categoryService.countCategories();
    }
}
