package org.zhuqigong.blogservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhuqigong.blogservice.model.AdminDetails;
import org.zhuqigong.blogservice.model.Post;
import org.zhuqigong.blogservice.model.ResponseBuilder;
import org.zhuqigong.blogservice.model.User;
import org.zhuqigong.blogservice.service.AdminDetailsService;
import org.zhuqigong.blogservice.service.AdminService;
import org.zhuqigong.blogservice.service.PostService;
import org.zhuqigong.blogservice.util.JwtUtils;

import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;
    private final PostService postService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AdminDetailsService adminDetailsService;
    private final PasswordEncoder bCryptPasswordEncoder;

    public AdminController(AdminService adminService, PostService postService, AuthenticationManager authenticationManager, JwtUtils jwtUtils, AdminDetailsService adminDetailsService, PasswordEncoder bCryptPasswordEncoder) {
        this.adminService = adminService;
        this.postService = postService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.adminDetailsService = adminDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/signIn")
    public Map<String, Object> login(@RequestParam String username, @RequestParam String password) {
        User user = adminService.findUserByUsername(username).orElse(null);
        if (user == null) {
            return new ResponseBuilder().status(200).message("User Not Found").build();
        } else {
            if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
                //session settings
                AdminDetails adminDetails = adminDetailsService.loadUserByUsername(user.getUsername());
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(adminDetails, password, adminDetails.getAuthorities()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);
                return new ResponseBuilder().status(200).message("Sign in success")
                        .append("username", adminDetails.getUsername())
                        .append("accessToken", jwt)
                        .build();
            } else {
                return new ResponseBuilder().status(200).message("Login Failed,Password Not Right").build();
            }
        }
    }

    @GetMapping("/posts")
    @PreAuthorize("hasAnyAuthority({'ADMIN','USER'})")
    public Map<String, Object> findPosts(@RequestParam int page, @RequestParam int size) {
        return postService.getPosts(page, size);
    }

    @PostMapping("/post/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> createNewPost(@RequestParam(required = false) Long id, @RequestParam String title,
                                             @RequestParam String content) {
        Post p = new Post();
        if (null != id) {
            p.setId(id);
        }
        p.setTitle(title);
        p.setContent(content);
        return postService.saveOrUpdatePost(p);
    }

    @DeleteMapping("/post/delete/id/{postId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> deletePost(@PathVariable Long postId) {
        return postService.deletePost(postId);
    }

    @PostMapping("/markdown/compileToHtml")
    @ResponseBody
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> compileMarkdownText2Html(@RequestParam String markdownText) {
        return adminService.formatMarkdownText2Html(markdownText);
    }

    @PostMapping("/signOut")
    public Map<String, Object> signOut() {
        return null;
    }

    @PostMapping("/post/upload")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, Object> upload(@RequestParam MultipartFile[] files) {
        try {
            adminService.saveMarkdownFile(files);
            return new ResponseBuilder().message("Upload markdown file success").build();
        } catch (Exception e) {
            return new ResponseBuilder().message("Upload markdown error").build();
        }
    }
}
