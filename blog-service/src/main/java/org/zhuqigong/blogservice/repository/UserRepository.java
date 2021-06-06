package org.zhuqigong.blogservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.zhuqigong.blogservice.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
