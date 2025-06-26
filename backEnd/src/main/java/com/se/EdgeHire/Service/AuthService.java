package com.se.EdgeHire.Service;

import com.se.EdgeHire.Entity.User;
import com.se.EdgeHire.Repository.UserRepository;
import com.se.EdgeHire.Util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); // 新增

    // 登录
    public Optional<User> login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) { //密码匹配
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
        user.setPassword(passwordEncoder.encode(user.getPassword())); // 哈希算法加密
        return userRepository.save(user);
    }

    public String generateToken(int userId){
        return JwtUtil.generateToken(userId);
    }
}