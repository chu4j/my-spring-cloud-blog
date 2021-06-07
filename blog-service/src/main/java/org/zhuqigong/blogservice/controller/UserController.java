package org.zhuqigong.blogservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.zhuqigong.blogservice.model.*;
import org.zhuqigong.blogservice.service.AdminDetailsService;
import org.zhuqigong.blogservice.service.PostService;
import org.zhuqigong.blogservice.service.UserService;
import org.zhuqigong.blogservice.util.JwtUtils;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
public class UserController {
    private final UserService userService;
    private final PostService postService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AdminDetailsService adminDetailsService;
    @Autowired
    private PasswordEncoder bCryptPasswordEncoder;

    public UserController(UserService userService, PostService postService) {
        this.userService = userService;
        this.postService = postService;
    }

    @PostMapping("/signIn")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<?> login(HttpServletRequest request,
                                   @RequestParam String username,
                                   @RequestParam String password) {
        User user = userService.findUserByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.ok("User Not Found");
        } else {
            if (bCryptPasswordEncoder.matches(password, user.getPassword())){
                //session settings
                AdminDetails adminDetails = adminDetailsService.loadUserByUsername(user.getUsername());
                Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(adminDetails,password, adminDetails.getAuthorities()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);
                return ResponseEntity.ok(new JwtResponse(adminDetails.getUsername(), jwt));
            } else{
                return ResponseEntity.ok("Login Failed,Password Not Right");
            }
        }
    }

    @GetMapping("/posts")
    public PagePost findPosts(@RequestParam int page, @RequestParam int size) {
        return postService.getPosts(page, size);
    }

    @PostMapping("/post/create")
    @ResponseBody
    public Post createNewPost(@RequestParam(required = false) Long id, @RequestParam String title,
                              @RequestParam String content) {
        Post p = new Post();
        if (null != id) {
            p.setId(id);
        }
        p.setTitle(title);
        p.setContent(content);
        return postService.createPost(p);
    }

    @DeleteMapping("/post/delete/id/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long postId) {
        return postService.deletePost(postId);
    }

    @PreAuthorize(value = "hasAnyRole('ROLE_USER')")
    @PostMapping("/markdown/compileToHtml")
    @ResponseBody
    public ResponseEntity<String> compileMarkdownText2Html(@RequestParam String markdownText) {
        return postService.formatMarkdownText2Html(markdownText);
    }

    @PostMapping("/signOut")
    public ResponseEntity<String> signOut() {
        return null;
    }
}
