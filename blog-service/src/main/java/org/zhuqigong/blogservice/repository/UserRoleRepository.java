package org.zhuqigong.blogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zhuqigong.blogservice.model.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
}
