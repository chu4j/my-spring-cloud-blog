package org.zhuqigong.blogservice.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.AdminDetails;
import org.zhuqigong.blogservice.model.Role;
import org.zhuqigong.blogservice.model.User;

import java.util.Collections;

@Service
public class AdminDetailsService implements UserDetailsService {
    private final UserService userService;

    public AdminDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public AdminDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username Not Found"));
        return new AdminDetails(user.getUsername(), user.getPassword(), Collections.singletonList(new Role("ROLE_USER")));
    }
}
