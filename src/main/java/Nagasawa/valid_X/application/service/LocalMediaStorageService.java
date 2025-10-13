package Nagasawa.valid_X.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

// ローカルにディレクトリを配置している設定です。
// アプリを運用する際は、mediaの変換をこのサービス内で行うのはトランザクションの遅れの原因となります
// なので、フロント側でなんとかしましょう
@Service
@RequiredArgsConstructor
public class LocalMediaStorageService {

    @Value("${app.storage.base-path}")
    private String basePath;

    @Value("${app.storage.public-base-url}")
    private String publicBaseUrl;

    public String saveBytes(byte[] bytes, String suggestedFilename) {
        try {
            Path storageDir = Paths.get(basePath);
            if (!Files.exists(storageDir)) Files.createDirectories(storageDir);

            String filename = UUID.randomUUID() + "_" + (suggestedFilename != null ? suggestedFilename : "upload.bin");
            Path target = storageDir.resolve(filename);
            Files.write(target, bytes);
            // storage_keyとしてDBに保存
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save bytes to file", e);
        }
    }

    public String saveDataUrl(String dataUrl, String suggestedName) {
        try {
            if (dataUrl == null || dataUrl.isBlank()) {
                throw new IllegalArgumentException("dataUrl is null or blank");
            }
            if (!dataUrl.startsWith("data:")) {
                throw new IllegalArgumentException("Invalid data URL: missing 'data:' prefix");
            }

            int comma = dataUrl.indexOf(',');
            if (comma < 0) {
                throw new IllegalArgumentException("Invalid data URL: missing comma separator");
            }

            String base64 = dataUrl.substring(comma + 1);
            if (base64.isBlank()) {
                throw new IllegalArgumentException("Invalid data URL: empty base64 payload");
            }

            byte[] bytes;
            try {
                bytes = java.util.Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid data URL: base64 decode failed", e);
            }

            // saveBytes は IOException を投げない（RuntimeException に包む）実装になっている
            return saveBytes(bytes, suggestedName);

        } catch (RuntimeException re) {
            throw re; // 具体的な原因をそのまま上位へ
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected error while saving data URL", ex);
        }
    }

    public String save(MultipartFile file) {
        try {
            Path storageDir = Paths.get(basePath);
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = storageDir.resolve(filename);
            file.transferTo(target);

            // DB の storage_key に対応（ここではローカルパス）
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save multipart file", e);
        }
    }

    public String buildPublicUrl(String storageKey) {
        return publicBaseUrl + storageKey;
    }

    public Resource loadAsResource(String storageKey) {
        Path file = Paths.get(basePath).resolve(storageKey);
        return Files.exists(file) ? new FileSystemResource(file) : null;
    }

    public String detectMimeType(String storageKey) {
        try {
            Path file = Paths.get(basePath).resolve(storageKey);
            return Files.probeContentType(file);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}