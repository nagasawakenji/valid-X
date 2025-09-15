package Nagasawa.valid_X.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

/**
 * Result of a verification attempt.
 * <p>
 * Encapsulates both the status of the verification and, when applicable,
 * the issued authentication token and its expiry.
 */
public record VerifyResult(
        VerifyStatus status
) {
    public static VerifyResult alreadyVerified() {
        return new VerifyResult(VerifyStatus.ALREADY_VERIFIED);
    }

    public static VerifyResult expiredOrInvalid() {
        return new VerifyResult(VerifyStatus.EXPIRED_OR_INVALID);
    }

    public static VerifyResult loginMailEnqueued() {
        return new VerifyResult(VerifyStatus.LOGIN_MAIL_ENQUEUED);
    }

    public enum VerifyStatus {
        ALREADY_VERIFIED,
        EXPIRED_OR_INVALID,
        LOGIN_MAIL_ENQUEUED
    }
}
