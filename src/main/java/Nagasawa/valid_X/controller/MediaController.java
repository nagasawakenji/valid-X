package Nagasawa.valid_X.controller;

import Nagasawa.valid_X.application.service.LocalMediaStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Slf4j
public class MediaController {

    private final LocalMediaStorageService localMediaStorageService;

    // MediaController
    @GetMapping("/{storageKey}")
    public ResponseEntity<Resource> getMedia(@PathVariable String storageKey, @AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        Resource resource = localMediaStorageService.loadAsResource(storageKey);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        String mimeType = localMediaStorageService.detectMimeType(storageKey);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .body(resource);
    }
}