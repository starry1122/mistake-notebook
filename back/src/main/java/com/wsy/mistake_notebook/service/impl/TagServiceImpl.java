package com.wsy.mistake_notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wsy.mistake_notebook.dto.TagCreateDTO;
import com.wsy.mistake_notebook.entity.Tag;
import com.wsy.mistake_notebook.mapper.TagMapper;
import com.wsy.mistake_notebook.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;

    public TagServiceImpl(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    @Override
    public List<Tag> listByUserId(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .orderByAsc(Tag::getId);
        return tagMapper.selectList(wrapper);
    }

    @Override
    public Tag create(Long userId, TagCreateDTO dto) {
        String name = dto.getName() == null ? null : dto.getName().trim();
        if (!StringUtils.hasText(name)) {
            throw new RuntimeException("Tag name cannot be empty");
        }

        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .eq(Tag::getName, name);

        Tag existing = tagMapper.selectOne(wrapper);
        if (existing != null) {
            return existing;
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);
        tagMapper.insert(tag);
        return tag;
    }

    @Override
    public void deleteByUserId(Long userId, Long tagId) {
        Tag tag = tagMapper.selectById(tagId);
        if (tag == null) {
            return;
        }
        if (!userId.equals(tag.getUserId())) {
            throw new RuntimeException("No permission to delete this tag");
        }
        tagMapper.deleteById(tagId);
    }
}
