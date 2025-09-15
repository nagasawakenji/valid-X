package Nagasawa.valid_X.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResendVerificationRequest {

    @Email
    @Size(max = 254)
    String email;

    @JsonProperty("username_or_email")
    @Size(max = 254)
    String usernameOrEmail;

    // oneOf: どちらか一方のみ（両方nullは不可、両方セットも不可）
    @AssertTrue(message = "either 'email' or 'username_or_email' must be set (but not both)")
    public boolean isExactlyOnePresent() {
        boolean hasEmail = email != null && !email.isBlank();
        boolean hasUserOrEmail = usernameOrEmail != null && !usernameOrEmail.isBlank();
        return hasEmail ^ hasUserOrEmail;  // XOR: 片方だけtrue
    }
}