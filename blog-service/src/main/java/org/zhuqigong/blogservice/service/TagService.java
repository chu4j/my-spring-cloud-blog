package org.zhuqigong.blogservice.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.repository.TagRepository;

@Service
public class TagService {
  private final TagRepository tagRepository;

  public TagService(TagRepository tagRepository) {
    this.tagRepository = tagRepository;
  }

  public List<Tag> findTags() {
    return tagRepository.findAll();
  }
}
