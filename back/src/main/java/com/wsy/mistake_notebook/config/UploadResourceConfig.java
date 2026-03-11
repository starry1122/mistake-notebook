package com.wsy.mistake_notebook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

    private final MistakeModuleProperties props;

    public UploadResourceConfig(MistakeModuleProperties props) {
        this.props = props;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String dir = props.getUploadDir();
        if (!StringUtils.hasText(dir)) {
            dir = "uploads";
        }

        // 统一转为绝对路径，避免相对路径在不同启动目录下产生歧义
        Path uploadPath = Path.of(dir).toAbsolutePath().normalize();

        // /uploads/** -> 本地 uploadDir 目录
        // 注意：用 toUri().toString() 生成 file:/...，兼容 Windows / Linux
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath.toUri().toString());
    }
}
