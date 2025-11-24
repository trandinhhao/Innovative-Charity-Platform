package dev.lhs.charity_backend.configuration;

import com.cloudinary.Cloudinary;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        String cloudinaryUrl = System.getenv("CLOUDINARY_URL");
        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            cloudinaryUrl = dotenv.get("CLOUDINARY_URL");
        }

        if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
            throw new IllegalStateException("Missing CLOUDINARY_URL in environment or .env file");
        }

        return new Cloudinary(cloudinaryUrl);
    }

}
