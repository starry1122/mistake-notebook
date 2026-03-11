package com.wsy.mistake_notebook.service;

import com.wsy.mistake_notebook.dto.TagCreateDTO;
import com.wsy.mistake_notebook.entity.Tag;

import java.util.List;

public interface TagService {

    List<Tag> listByUserId(Long userId);

    Tag create(Long userId, TagCreateDTO dto);

    void deleteByUserId(Long userId, Long tagId);
}

