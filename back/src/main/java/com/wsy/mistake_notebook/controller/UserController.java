package com.wsy.mistake_notebook.controller;

import com.wsy.mistake_notebook.vo.Result;
import com.wsy.mistake_notebook.dto.LoginDTO;
import com.wsy.mistake_notebook.dto.RegisterDTO;
import com.wsy.mistake_notebook.entity.User;
import com.wsy.mistake_notebook.service.UserService;
import com.wsy.mistake_notebook.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success("注册成功");
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }

        token = token.substring(7);

        Long userId = JwtUtil.getUserId(token);

        return Result.success(userService.getById(userId));
    }

    @PutMapping("/update")
    public Result<String> update(@RequestBody User user) {
        userService.update(user);
        return Result.success("修改成功");
    }
}
