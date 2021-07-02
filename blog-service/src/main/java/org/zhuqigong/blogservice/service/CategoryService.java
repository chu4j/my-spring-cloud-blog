package org.zhuqigong.blogservice.service;

import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.CategoryStatistics;
import org.zhuqigong.blogservice.repository.CategoryRepository;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(
            CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryStatistics> countCategories() {
        return categoryRepository.countCategories();
    }
}
