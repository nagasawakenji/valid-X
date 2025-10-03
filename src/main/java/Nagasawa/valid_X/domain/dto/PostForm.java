package Nagasawa.valid_X.domain.dto;

import java.util.List;

public record PostForm(
        String content,
        Long inReplyToTweet,
        List<MediaCreate> medias
) {}
