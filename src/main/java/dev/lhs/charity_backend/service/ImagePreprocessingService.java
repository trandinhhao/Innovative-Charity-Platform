package dev.lhs.charity_backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service xử lý tiền xử lý ảnh (Bước 4 trong pipeline)
 * - Resize/nén ảnh
 * - Trích metadata cơ bản
 */
public interface ImagePreprocessingService {
    
    // resize, zip, get metadata
    Map<String, Object> preprocessImage(MultipartFile file);
    
    // validate image: format, size,...
    boolean validateImage(MultipartFile file);
}

