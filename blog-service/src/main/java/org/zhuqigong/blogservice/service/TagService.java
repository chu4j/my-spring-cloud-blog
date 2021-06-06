package org.zhuqigong.blogservice.service;

import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.Tag;
import org.zhuqigong.blogservice.repository.TagRepository;

import java.util.List;

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
