package Nagasawa.valid_X.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginForm {

    @NotBlank
    @Size(min=3, max=254)
    private String usernameOrEmail;

    @NotBlank
    @Size(min=8, max=64)
    private String password;
}
