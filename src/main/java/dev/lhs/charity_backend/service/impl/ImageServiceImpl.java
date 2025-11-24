package dev.lhs.charity_backend.service.impl;

import com.cloudinary.Cloudinary;
import dev.lhs.charity_backend.enumeration.ErrorCode;
import dev.lhs.charity_backend.exception.AppException;
import dev.lhs.charity_backend.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(), Map.of("folder", "spring_uploads"));
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new AppException(ErrorCode.IMAGE_UPLOAD_FAIL);
        }
    }

}
