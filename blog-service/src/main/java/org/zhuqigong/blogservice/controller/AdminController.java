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
import org.zhuqigong.blogservice.model.*;
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
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        User user = adminService.findUserByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.ok("User Not Found");
        } else {
            if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
                //session settings
                AdminDetails adminDetails = adminDetailsService.loadUserByUsername(user.getUsername());
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(adminDetails, password, adminDetails.getAuthorities()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);
                return ResponseEntity.ok(new JwtResponseEntity(adminDetails.getUsername(), jwt));
            } else {
                return ResponseEntity.ok("Login Failed,Password Not Right");
            }
        }
    }

    @GetMapping("/posts")
    @PreAuthorize("hasAnyAuthority({'ADMIN','USER'})")
    public PostResponseEntity findPosts(@RequestParam int page, @RequestParam int size) {
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
    public Map<String, ?> signOut() {
        return null;
    }

    @PostMapping("/post/upload")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Map<String, ?> upload(@RequestParam MultipartFile[] files) {
        try {
            adminService.saveMarkdownFile(files);
            return new ResponseBuilder().append("message", "Upload markdown file success").build();
        } catch (Exception e) {
            return new ResponseBuilder().append("message", "Upload markdown error").build();
        }
    }
}
