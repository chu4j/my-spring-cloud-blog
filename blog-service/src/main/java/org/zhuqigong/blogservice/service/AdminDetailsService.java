package org.zhuqigong.blogservice.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.AdminDetails;
import org.zhuqigong.blogservice.model.User;

@Service
public class AdminDetailsService implements UserDetailsService {
    private final AdminService adminService;

    public AdminDetailsService(AdminService adminService) {
        this.adminService = adminService;
    }

    @Override
    public AdminDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = adminService.findUserByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Username Not Found"));
        return new AdminDetails(user.getUsername(), user.getPassword(), user.getUserRoles());
    }
}
