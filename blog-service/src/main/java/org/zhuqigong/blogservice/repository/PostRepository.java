package org.zhuqigong.blogservice.repository;

import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.zhuqigong.blogservice.model.Category;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.Tag;

public interface PostRepository extends JpaRepository<Post, Long> {
  Optional<Post> findPostByTitle(String postTitle);

  Optional<Page<Post>> findPostByCategoriesIn(Pageable pageable, Collection<Category> categories);

  long countPostByCategoriesIn(Collection<Category> categories);

  Optional<Page<Post>> findPostByTagsIn(Pageable pageable, Collection<Tag> tags);

  long countPostByTagsIn(Collection<Tag> tags);

  Post findFirstByPublishTimeAfterOrderByPublishTimeAsc(Date date);

  Post findFirstByPublishTimeBeforeOrderByPublishTimeDesc(Date date);
}
