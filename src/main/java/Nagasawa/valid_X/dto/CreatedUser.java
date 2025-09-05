package Nagasawa.valid_X.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class CreatedUser {

    String userId;
    String username;
    String displayName;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant createdAt;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String email;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String token;

    @Value
    @Builder
    public static class Profile {
        String bio;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String avatarUrl;
        @JsonProperty("protected")
        boolean protected_;
    }

    @Value
    @Builder
    public static class Counts {
        int followers;
        int following;
        int tweets;
    }
}
