package Nagasawa.valid_X.domain.dto;

import lombok.Value;

@Value
public class UserSummary {
    Long id;
    String username;
    String displayName;
    String locale;
    String timezone;
    String avatarUrl;
}