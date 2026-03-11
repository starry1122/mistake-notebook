package com.wsy.mistake_notebook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("review_record")
public class ReviewRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("question_id")
    private Long questionId;

    @TableField("review_count")
    private Integer reviewCount;

    @TableField("last_review_time")
    private LocalDateTime lastReviewTime;

    @TableField("next_review_time")
    private LocalDateTime nextReviewTime;

    @TableField("mastery_level")
    private Integer masteryLevel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public LocalDateTime getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(LocalDateTime lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }

    public LocalDateTime getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(LocalDateTime nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    public Integer getMasteryLevel() {
        return masteryLevel;
    }

    public void setMasteryLevel(Integer masteryLevel) {
        this.masteryLevel = masteryLevel;
    }
}

