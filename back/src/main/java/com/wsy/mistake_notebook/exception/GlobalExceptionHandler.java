package com.wsy.mistake_notebook.exception;

import com.wsy.mistake_notebook.vo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handle(Exception e) {
        return Result.fail(e.getMessage());
    }
}
