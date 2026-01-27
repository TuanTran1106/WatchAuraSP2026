package com.example.watchaura.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    /**
     * Upload ảnh và lưu vào thư mục uploads
     * @param file File ảnh cần upload
     * @return Đường dẫn file (tương đối) để lưu vào database
     */
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File không được để trống");
        }

        // Kiểm tra định dạng file
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh (image/*)");
        }

        try {
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);

            // Tạo tên file duy nhất
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + fileExtension;

            // Lưu file
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // Trả về đường dẫn tương đối
            return "/uploads/" + newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi upload file: " + e.getMessage());
        }
    }

    /**
     * Xóa file ảnh
     * @param filePath Đường dẫn file (tương đối)
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isEmpty() || filePath.equals("/uploads/")) {
            return;
        }

        try {
            // Chuyển đường dẫn tương đối thành tuyệt đối
            String filename = filePath.replace("/uploads/", "");
            Path fullPath = Paths.get(uploadDir).toAbsolutePath().resolve(filename);

            Files.deleteIfExists(fullPath);
        } catch (Exception e) {
            // Log lỗi nhưng không throw exception
            System.err.println("Lỗi khi xóa file: " + e.getMessage());
        }
    }
}
