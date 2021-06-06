package org.zhuqigong.blogservice.service;

import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findCategories() {
        return categoryRepository.findAll();
    }
}
