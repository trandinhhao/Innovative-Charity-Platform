package dev.lhs.charity_backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Service xử lý tiền xử lý ảnh (Bước 4 trong pipeline)
 * - Resize/nén ảnh
 * - Trích metadata cơ bản
 */
public interface ImagePreprocessingService {
    
    /**
     * Xử lý ảnh: resize, nén, và trích metadata
     * @param file File ảnh gốc
     * @return Map chứa metadata (width, height, size, format, etc.)
     */
    Map<String, Object> preprocessImage(MultipartFile file);
    
    /**
     * Validate ảnh có hợp lệ không (format, size, etc.)
     * @param file File ảnh
     * @return true nếu hợp lệ
     */
    boolean validateImage(MultipartFile file);
}

