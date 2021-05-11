package org.zhuqigong.blogservice.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zhuqigong.blogservice.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  Optional<List<Category>> findByCategory(String categoryName);
}
