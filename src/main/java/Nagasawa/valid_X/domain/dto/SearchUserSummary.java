package Nagasawa.valid_X.domain.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchUserSummary {
    /** ユーザーID */
    private Long id;

    /** ユーザー名（@username） */
    private String username;

    /** 表示名 */
    private String displayName;

    /** アカウント作成時間 */
    private Instant createdAt;

    /** タイムゾーン */
    private String timezone;

    /** ロケール */
    private String locale;

    /** 自己紹介文 */
    private String bio;

    /** アバターURL */
    private String avatarUrl;

    /** フォロワー数 */
    private int followers;

    /** フォロー数 */
    private int following;

    /** フォロー状態（オプション） */
    private Boolean isFollowed;
}
