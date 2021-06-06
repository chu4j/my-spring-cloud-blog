package org.zhuqigong.blogservice.model;

import org.springframework.security.core.GrantedAuthority;

public class Role implements GrantedAuthority {
    private final String roleName;

    public Role(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String getAuthority() {
        return roleName;
    }
}
