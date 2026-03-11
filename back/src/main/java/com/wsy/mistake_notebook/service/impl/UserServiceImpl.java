package com.wsy.mistake_notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsy.mistake_notebook.dto.LoginDTO;
import com.wsy.mistake_notebook.dto.RegisterDTO;
import com.wsy.mistake_notebook.entity.User;
import com.wsy.mistake_notebook.mapper.UserMapper;
import com.wsy.mistake_notebook.service.UserService;
import com.wsy.mistake_notebook.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 注入密码加密器（来自 SecurityConfig）
     */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    @Override
    public void register(RegisterDTO dto) {

        // 1. 判断用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());

        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户对象
        User user = new User();
        user.setUsername(dto.getUsername());

        // 3. 密码加密
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodedPassword);

        // 4. 默认角色
        user.setRole("student");

        // 5. 插入数据库
        userMapper.insert(user);
    }

    /**
     * 用户登录
     */
    @Override
    public String login(LoginDTO dto) {

        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());

        User user = userMapper.selectOne(wrapper);

        // 2. 判断用户是否存在
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 3. 校验密码
        boolean matches = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if (!matches) {
            throw new RuntimeException("密码错误");
        }

        // 4. 生成 JWT Token
        return JwtUtil.generateToken(user.getId(), user.getRole());
    }

    /**
     * 根据 ID 查询用户
     */
    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 更新用户信息
     */
    @Override
    public void update(User user) {
        userMapper.updateById(user);
    }
}
