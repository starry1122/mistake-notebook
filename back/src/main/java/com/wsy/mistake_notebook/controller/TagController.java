package com.wsy.mistake_notebook.controller;

import com.wsy.mistake_notebook.dto.TagCreateDTO;
import com.wsy.mistake_notebook.entity.Tag;
import com.wsy.mistake_notebook.service.TagService;
import com.wsy.mistake_notebook.utils.AuthUtil;
import com.wsy.mistake_notebook.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/student/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Result<List<Tag>> listMyTags(HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        return Result.success(tagService.listByUserId(userId));
    }

    @PostMapping
    public Result<Tag> createTag(@Valid @RequestBody TagCreateDTO dto, HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        return Result.success(tagService.create(userId, dto));
    }

    @DeleteMapping("/{tagId}")
    public Result<String> deleteTag(@PathVariable Long tagId, HttpServletRequest request) {
        Long userId = AuthUtil.requireStudentUserId(request);
        tagService.deleteByUserId(userId, tagId);
        return Result.success("Deleted");
    }
}
