package de.uniba.dsg.serverless.users;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationForm {

    @NotNull
    @Size(min = 8)
    @Pattern(regexp = "[a-z]*", message = "username only contains at least 8 lower case letters")
    private String username;
    @PasswordMatch
    private PasswordForm passwordForm;
    @NotNull
    @NotEmpty
    private String fullName;
    @Email
    private String mail;

    public User toUser(PasswordEncoder passwordEncoder, String role) {
        return new User(this.username, passwordEncoder.encode(this.passwordForm.getPassword()), this.fullName, role, this.mail);
    }
}
