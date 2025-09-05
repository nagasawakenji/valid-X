package Nagasawa.valid_X.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Problem {

    // エラー時のレスポンス
    // 必須フィールド
    /** 例: "https://api.example.com/problems/validation_failed" */
    private String type;

    /** 短い要約: "Validation failed" */
    private String title;

    /** HTTP ステータスコード（例: 400, 401, 403, 404, 409, 422 など） */
    private int Status;

    // 任意フィールド
    /** 詳細メッセージ（任意） */
    private String detail;
    /** フィールドごとのエラー明細 */
    private List<ErrorDetail> errors;
}
