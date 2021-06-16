package org.zhuqigong.blogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zhuqigong.blogservice.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<List<Tag>> findByTagName(String tagName);
}
