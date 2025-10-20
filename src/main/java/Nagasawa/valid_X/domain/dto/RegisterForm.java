package Nagasawa.valid_X.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RegisterForm {
    // /v1/auth/signupで使用
    @NotBlank
    @Size(min=3, max=30)
    @Pattern(regexp = "^(?!_)(?!.*__)[a-z0-9_]+(?<!_)$")
    private String username;

    @NotBlank
    @Size(min=1, max=50)
    private String displayName;

    @NotBlank
    @Email
    @Size(max=254)
    private String email;

    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9])[ -~]{8,64}$")
    private String password;

    @Size(max=35)
    private String locale;

    @Size(max=50)
    private String timezone;
}
