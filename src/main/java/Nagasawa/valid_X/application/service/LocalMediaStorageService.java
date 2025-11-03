package Nagasawa.valid_X.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // ★ Slf4jを追加
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
@Slf4j // ★ Slf4jを有効化
public class LocalMediaStorageService {

    @Value("${app.storage.base-path}")
    private String basePath;

    @Value("${app.storage.public-base-url}")
    private String publicBaseUrl;

    public String saveBytes(byte[] bytes, String suggestedFilename) {
        log.debug("Attempting to save bytes (Legacy method) with suggested filename: {}", suggestedFilename);
        try {
            Path storageDir = Paths.get(basePath);
            if (!Files.exists(storageDir)) {
                log.debug("Storage directory does not exist, creating: {}", basePath);
                Files.createDirectories(storageDir);
            }

            String filename = UUID.randomUUID() + "_" + (suggestedFilename != null ? suggestedFilename : "upload.bin");
            Path target = storageDir.resolve(filename);
            Files.write(target, bytes);
            log.debug("Successfully saved bytes to: {}", target.toAbsolutePath());

            // storage_keyとしてDBに保存
            return filename;
        } catch (IOException e) {
            log.warn("Failed to save bytes to file at {}. Check Docker volume and permissions.", basePath, e);
            throw new RuntimeException("Failed to save bytes to file", e);
        }
    }

    public String saveDataUrl(String dataUrl, String suggestedName) {
        // Base64処理は非推奨のため、ログ出力のみ追加
        log.debug("Attempting to save data URL (Legacy method).");
        try {
            if (dataUrl == null || dataUrl.isBlank()) {
                throw new IllegalArgumentException("dataUrl is null or blank");
            }
            // ... (Base64デコード処理は省略) ...

            int comma = dataUrl.indexOf(',');
            String base64 = dataUrl.substring(comma + 1);

            byte[] bytes;
            try {
                bytes = java.util.Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException e) {
                log.warn("Base64 decode failed for data URL.", e);
                throw new IllegalArgumentException("Invalid data URL: base64 decode failed", e);
            }

            // saveBytes を使用
            return saveBytes(bytes, suggestedName);

        } catch (RuntimeException re) {
            log.warn("RuntimeException caught in saveDataUrl.", re);
            throw re;
        } catch (Exception ex) {
            log.error("Unexpected error while saving data URL.", ex);
            throw new RuntimeException("Unexpected error while saving data URL", ex);
        }
    }

    public String save(MultipartFile file) {
        log.debug("Attempting to save MultipartFile: OriginalFilename={}", file.getOriginalFilename());
        try {
            Path storageDir = Paths.get(basePath);
            log.debug("Resolved storage directory path: {}", storageDir.toAbsolutePath());

            if (!Files.exists(storageDir)) {
                log.debug("Storage directory does not exist, creating: {}", basePath);
                Files.createDirectories(storageDir);
            }

            // ファイル名が null や空文字の場合に備えて安全策をとる
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                log.warn("MultipartFile has null or blank original filename. Using 'upload.bin'.");
                originalFilename = "upload.bin";
            }

            // ファイル名生成 (UUID + オリジナルファイル名)
            String filename = UUID.randomUUID() + "_" + originalFilename;
            Path target = storageDir.resolve(filename);
            log.debug("Target file path resolved to: {}", target.toAbsolutePath());

            // ファイルの書き込み (Tomcat transferTo を使用)
            file.transferTo(target);

            log.debug("Successfully saved MultipartFile. Storage Key: {}", filename);

            // DB の storage_key に対応（ここではローカルパス）
            return filename;
        } catch (IOException e) {
            // ★ IOExceptionの詳細をWARNレベルでログ出力
            log.warn("Failed to save MultipartFile to path: {}. Check Docker volume and permissions.", basePath, e);
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
