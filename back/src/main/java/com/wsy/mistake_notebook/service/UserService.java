package com.wsy.mistake_notebook.service;

import com.wsy.mistake_notebook.dto.LoginDTO;
import com.wsy.mistake_notebook.dto.RegisterDTO;
import com.wsy.mistake_notebook.entity.User;

public interface UserService {

    void register(RegisterDTO dto);

    String login(LoginDTO dto);

    User getById(Long id);

    void update(User user);
}
