package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    // 登录
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt;
        }
        return Optional.empty();
    }

    // 注册
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if ((user.getRole() != 0 && user.getRole() != 1 && user.getRole() != 2)) {
            throw new RuntimeException("角色非法");
        }
        return userRepository.save(user);
    }
}