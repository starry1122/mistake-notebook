package com.wsy.mistake_notebook.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewSubmitDTO {

    @NotNull
    private Boolean correct;

    private String redoAnswer;
}

