package com.wsy.mistake_notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wsy.mistake_notebook.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
