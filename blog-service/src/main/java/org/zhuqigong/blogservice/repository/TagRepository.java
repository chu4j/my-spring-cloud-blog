package org.zhuqigong.blogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.model.TagStatistics;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<List<Tag>> findByTagName(String tagName);

    @Query(nativeQuery = true, value = "select TAG_NAME as tagName,count(TAG_NAME) as count from TAG group by TAG_NAME")
    List<TagStatistics> countTags();
}
