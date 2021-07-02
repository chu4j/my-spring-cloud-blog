package org.zhuqigong.blogservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.zhuqigong.blogservice.model.Post;

import java.util.Date;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findPostByTitle(String postTitle);

    @Query(nativeQuery = true,
            value = "select * from POST left join POST_CATEGORY on  POST.ID = POST_CATEGORY.POST_ID left join CATEGORY on POST_CATEGORY.CATEGORIES_ID=CATEGORY.ID where CATEGORY.CATEGORY_NAME=:categoryName order by PUBLISH_TIME desc ",
            countQuery = "select count(*) from POST left join POST_CATEGORY on  POST.ID = POST_CATEGORY.POST_ID left join CATEGORY on POST_CATEGORY.CATEGORIES_ID=CATEGORY.ID where CATEGORY.CATEGORY_NAME=:categoryName")
    Optional<Page<Post>> findPostByCategoriesIn(@Param("categoryName") String categoryName, Pageable pageable);

    @Query(
            nativeQuery = true,
            value = "select * from POST left join POST_TAG on  POST.ID = POST_TAG.POST_ID left join TAG on POST_TAG.TAGS_ID=TAG.ID where TAG.TAG_NAME=:tagName order by PUBLISH_TIME desc ",
            countQuery = "select count(*) from POST left join POST_TAG on  POST.ID = POST_TAG.POST_ID left join TAG on POST_TAG.TAGS_ID=TAG.ID where TAG.TAG_NAME=:tagName"
    )
    Optional<Page<Post>> findPostByTagsIn(@Param("tagName") String tagName, Pageable pageable);

    @Query(nativeQuery = true, value = "select count(*) from POST left join POST_TAG on  POST.ID = POST_TAG.POST_ID left join TAG on POST_TAG.TAGS_ID=TAG.ID where TAG.TAG_NAME=:tagName")
    long countPostByTagsIn(String tagName);

    Post findFirstByPublishTimeAfterOrderByPublishTimeAsc(Date date);

    Post findFirstByPublishTimeBeforeOrderByPublishTimeDesc(Date date);

    @Query(nativeQuery = true, value = "select count(*) from POST left join POST_CATEGORY on  POST.ID = POST_CATEGORY.POST_ID left join CATEGORY on POST_CATEGORY.CATEGORIES_ID=CATEGORY.ID where CATEGORY.CATEGORY_NAME=:categoryName")
    long countPostByCategoriesIn(String categoryName);
}
