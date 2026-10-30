package edu.campus.findit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${findit.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files under /uploads/**
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String pathStr = uploadPath.toString().replace('\\', '/');
        if (!pathStr.endsWith("/")) {
            pathStr += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + pathStr, "classpath:/uploads/");

        // Serve frontend resources
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}

