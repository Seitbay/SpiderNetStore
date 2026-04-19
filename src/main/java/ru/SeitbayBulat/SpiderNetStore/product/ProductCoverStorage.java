package ru.SeitbayBulat.SpiderNetStore.product;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

@Component
public class ProductCoverStorage {

    private static final long MAX_BYTES = 5 * 1024 * 1024;

    @Value("${app.upload.product-covers:uploads/product-covers}")
    private String uploadDir;

    @PostConstruct
    void ensureUploadDir() {
        try {
            Files.createDirectories(Paths.get(uploadDir).toAbsolutePath().normalize());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Сохраняет файл превью и возвращает относительный URL для фронта.
     */
    public String saveCover(long productId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл превью пустой");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Превью не больше 5 МБ");
        }
        String ext = resolveExtension(file);
        Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
        String filename = productId + ext;
        Path target = dir.resolve(filename);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Не удалось сохранить превью");
        }
        return "/api/files/product-covers/" + filename;
    }

    private static String resolveExtension(MultipartFile file) {
        String ct = file.getContentType();
        if (ct != null) {
            String lowerCt = ct.toLowerCase(Locale.ROOT);
            if (lowerCt.startsWith("image/jpeg") || lowerCt.startsWith("image/jpg")) {
                return ".jpg";
            }
            if (lowerCt.startsWith("image/png")) {
                return ".png";
            }
            if (lowerCt.startsWith("image/webp")) {
                return ".webp";
            }
            if (lowerCt.startsWith("image/gif")) {
                return ".gif";
            }
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            String n = name.toLowerCase(Locale.ROOT);
            if (n.endsWith(".jpg") || n.endsWith(".jpeg")) {
                return ".jpg";
            }
            if (n.endsWith(".png")) {
                return ".png";
            }
            if (n.endsWith(".webp")) {
                return ".webp";
            }
            if (n.endsWith(".gif")) {
                return ".gif";
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Допустимы изображения: JPEG, PNG, WebP, GIF");
    }
}
