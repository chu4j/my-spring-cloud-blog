package org.zhuqigong.blogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.CategoryStatistics;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<List<Category>> findByCategoryName(String categoryName);

    @Query(nativeQuery = true, value = "select CATEGORY.CATEGORY_NAME as categoryName,count(CATEGORY_NAME) as count from CATEGORY group by CATEGORY_NAME")
    List<CategoryStatistics> countCategories();
}
