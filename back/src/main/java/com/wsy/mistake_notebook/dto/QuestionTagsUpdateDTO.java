package com.wsy.mistake_notebook.dto;

import java.util.List;

public class QuestionTagsUpdateDTO {

    private List<Long> tagIds;
    private List<String> tagNames;

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }

    public List<String> getTagNames() { return tagNames; }
    public void setTagNames(List<String> tagNames) { this.tagNames = tagNames; }
}
