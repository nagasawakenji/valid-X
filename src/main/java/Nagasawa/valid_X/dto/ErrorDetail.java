package Nagasawa.valid_X.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorDetail {

    /** アプリ内のエラーコード（例: "username_already_exists"） */
    private String code;

    /** エラー対象のフィールド名（例: "username"） */
    private String field;

    /** 人間向けの説明（例: "This username is not available"） */
    private String message;
}
