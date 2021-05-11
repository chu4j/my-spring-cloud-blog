package org.zhuqigong.blogservice.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zhuqigong.blogservice.model.Tag;

public interface TagRepository extends JpaRepository<Tag, Long> {
  Optional<List<Tag>> findByTag(String tagName);
}
