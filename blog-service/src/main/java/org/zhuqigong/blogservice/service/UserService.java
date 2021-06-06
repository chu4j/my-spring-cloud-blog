package org.zhuqigong.blogservice.service;

import org.springframework.stereotype.Service;
import org.zhuqigong.blogservice.model.User;
import org.zhuqigong.blogservice.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
