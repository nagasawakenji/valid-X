package Nagasawa.valid_X.domain.dto;

public enum SignupStatus {
    ACCEPTED,        // 受付済み（検証メールを送信）
    DUPLICATE,       // 既に登録/保留中
    RATE_LIMITED,    // 送信頻度制限を超過（resend等）
    INVALID_INPUT    // バリデーションなど
}