package dev.lhs.charity_backend.service.impl;

import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.service.ImagePreprocessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation của ImagePreprocessingService
 * Xử lý resize, nén và trích metadata từ ảnh
 */
@Slf4j
@Service
public class ImagePreprocessingServiceImpl implements ImagePreprocessingService {

    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1920;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_FORMATS = {"image/jpeg", "image/jpg", "image/png", "image/webp"};

    @Override
    public Map<String, Object> preprocessImage(MultipartFile file) {
        try {
            // Validate trước
            if (!validateImage(file)) {
                throw new AppException(ErrorCode.IMAGE_UPLOAD_FAIL);
            }

            // Đọc ảnh
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (originalImage == null) {
                throw new AppException(ErrorCode.IMAGE_UPLOAD_FAIL);
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();

            // Tính toán kích thước mới (giữ tỷ lệ)
            int newWidth = originalWidth;
            int newHeight = originalHeight;
            
            if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
                double ratio = Math.min((double) MAX_WIDTH / originalWidth, (double) MAX_HEIGHT / originalHeight);
                newWidth = (int) (originalWidth * ratio);
                newHeight = (int) (originalHeight * ratio);
            }

            // Resize nếu cần
            BufferedImage resizedImage = originalImage;
            if (newWidth != originalWidth || newHeight != originalHeight) {
                resizedImage = resizeImage(originalImage, newWidth, newHeight);
                log.info("Image resized from {}x{} to {}x{}", originalWidth, originalHeight, newWidth, newHeight);
            }

            // Trích metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("originalWidth", originalWidth);
            metadata.put("originalHeight", originalHeight);
            metadata.put("width", newWidth);
            metadata.put("height", newHeight);
            metadata.put("format", file.getContentType());
            metadata.put("originalSize", file.getSize());
            metadata.put("resized", newWidth != originalWidth || newHeight != originalHeight);

            return metadata;

        } catch (IOException e) {
            log.error("Error preprocessing image: {}", e.getMessage(), e);
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAIL);
        }
    }

    @Override
    public boolean validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Image file is null or empty");
            return false;
        }

        // Kiểm tra format
        String contentType = file.getContentType();
        if (contentType == null) {
            log.warn("Image content type is null");
            return false;
        }

        boolean validFormat = false;
        for (String format : ALLOWED_FORMATS) {
            if (contentType.equalsIgnoreCase(format)) {
                validFormat = true;
                break;
            }
        }

        if (!validFormat) {
            log.warn("Invalid image format: {}", contentType);
            return false;
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("Image file too large: {} bytes", file.getSize());
            return false;
        }

        return true;
    }

    /**
     * Resize ảnh với chất lượng tốt
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        
        // Sử dụng rendering hints để có chất lượng tốt hơn
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        
        return resizedImage;
    }
}

