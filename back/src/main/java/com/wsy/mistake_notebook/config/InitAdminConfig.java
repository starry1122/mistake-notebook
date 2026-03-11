package com.wsy.mistake_notebook.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsy.mistake_notebook.entity.User;
import com.wsy.mistake_notebook.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class InitAdminConfig {

    @Bean
    CommandLineRunner initAdmin(UserMapper userMapper, PasswordEncoder encoder) {
        return args -> {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, "admin");

            User admin = userMapper.selectOne(wrapper);

            if (admin == null) {
                admin = new User();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole("admin");
                userMapper.insert(admin);
                return;
            }

            boolean needUpdate = false;

            if (!"admin".equals(admin.getRole())) {
                admin.setRole("admin");
                needUpdate = true;
            }

            if (!isBcryptPassword(admin.getPassword())) {
                admin.setPassword(encoder.encode("admin123"));
                needUpdate = true;
            }

            if (needUpdate) {
                userMapper.updateById(admin);
            }
        };
    }

    private boolean isBcryptPassword(String password) {
        if (!StringUtils.hasText(password)) {
            return false;
        }
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }
}
