package org.zhuqigong.blogservice.task;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.User;
import org.zhuqigong.blogservice.model.UserRole;
import org.zhuqigong.blogservice.repository.UserRepository;
import org.zhuqigong.blogservice.repository.UserRoleRepository;
import org.zhuqigong.blogservice.service.PostService;
import org.zhuqigong.blogservice.util.MarkdownUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApplicationReadyTask {
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReadyTask.class);
    private final PostService postService;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder encoder;
    @Value("${my.blog.markdown-file.dir}")
    private String path;
    @Value("${my.blog.admin.user}")
    private String defaultUser;
    @Value("${my.blog.admin.password}")
    private String defaultPassword;

    public ApplicationReadyTask(PostService postService, UserRepository userRepository, PasswordEncoder encoder, UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.postService = postService;
        this.encoder = encoder;
        this.userRoleRepository = userRoleRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadingMarkdownStartUp() throws IOException {
        List<File> mdFiles = Files.walk(Paths.get(path), 8)
                .map(Path::toFile)
                .filter(file -> file.getName().endsWith(".md"))
                .collect(Collectors.toList());
        for (File file : mdFiles) {
            String markdownTitle = file.getName().replace(".md", "");
            String markdownText = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            Post post = MarkdownUtil.format(markdownTitle, markdownText);
            postService.createOrUpdatePost(post);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setUpAdminUserTask() {
        UserRole adminRole = new UserRole("ADMIN");
        userRoleRepository.save(adminRole);
        User admin = new User(defaultUser, encoder.encode(defaultPassword));
        admin.setUserRoles(Collections.singletonList(adminRole));
        userRepository.save(admin);
        LOG.info("Default User save success");
    }
}
