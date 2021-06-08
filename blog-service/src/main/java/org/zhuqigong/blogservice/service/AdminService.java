package org.zhuqigong.blogservice.service;

import org.apache.commons.io.IOUtils;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.User;
import org.zhuqigong.blogservice.repository.UserRepository;
import org.zhuqigong.blogservice.util.MarkdownUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final PostService postService;

    public AdminService(UserRepository userRepository, PostService postService) {
        this.userRepository = userRepository;
        this.postService = postService;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public ResponseEntity<String> formatMarkdownText2Html(String markdownText) {
        try {
            return ResponseEntity.ok(MarkdownUtil.formatMarkdown2Html(markdownText, false));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Markdown text format to html failed : " + e.getMessage());
        }
    }

    public void saveMarkdownFile(MultipartFile[] files) {
        Arrays.stream(files)
                .filter(file -> null != file.getOriginalFilename() && file.getOriginalFilename().endsWith(".md"))
                .map(file -> {
                    try {
                        return Pair.of(file.getOriginalFilename().replace(".md", ""), IOUtils.toString(file.getInputStream(), StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).forEach(p -> {
            if (null != p) {
                Post post = MarkdownUtil.format(p.getFirst(), p.getSecond());
                postService.saveOrUpdatePost(post);
            }
        });
    }
}
