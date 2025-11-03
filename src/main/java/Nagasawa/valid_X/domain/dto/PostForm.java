package Nagasawa.valid_X.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record PostForm(
        String content,
        Long inReplyToTweet
) {}
