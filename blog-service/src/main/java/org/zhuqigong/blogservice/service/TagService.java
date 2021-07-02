package org.zhuqigong.blogservice.service;

import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.TagStatistics;
import org.zhuqigong.blogservice.repository.TagRepository;

import java.util.List;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagStatistics> countTags() {
        return tagRepository.countTags();
    }
}
