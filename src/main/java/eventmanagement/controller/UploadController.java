package eventmanagement.controller;

import eventmanagement.dto.ApiResponse;
import eventmanagement.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/** Upload ảnh sự kiện. Nằm dưới /api/organizer/** nên SecurityConfig chỉ cho ROLE_ORGANIZER. */
@RestController
public class UploadController {

    private static final long MAX_BYTES = 5L * 1024 * 1024;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/api/organizer/uploads/event-image")
    public ApiResponse<Map<String, String>> uploadEventImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn file ảnh.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("Ảnh tối đa 5MB.");
        }
        // Kiểm tra nội dung thật là ảnh (không tin đuôi file / Content-Type do client gửi)
        String ext;
        try (InputStream in = file.getInputStream()) {
            var readers = ImageIO.getImageReaders(ImageIO.createImageInputStream(in));
            if (!readers.hasNext()) {
                throw new BadRequestException("File không phải ảnh hợp lệ (chỉ nhận JPG, PNG, GIF, WEBP).");
            }
            String format = readers.next().getFormatName().toLowerCase();
            switch (format) {
                case "jpeg", "jpg" -> ext = "jpg";
                case "png" -> ext = "png";
                case "gif" -> ext = "gif";
                default -> throw new BadRequestException("Chỉ nhận ảnh JPG, PNG hoặc GIF.");
            }
        }
        try (InputStream in = file.getInputStream()) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new BadRequestException("Không đọc được ảnh.");
            }
        }

        Path dir = Paths.get(uploadDir, "events").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        String name = UUID.randomUUID() + "." + ext; // tên ngẫu nhiên, không dùng tên client gửi
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        }
        return ApiResponse.success("Tải ảnh lên thành công.", Map.of("url", "/uploads/events/" + name));
    }
}
