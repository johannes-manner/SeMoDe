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
    @NotNull
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
            message = "at least 8 chars, containing digits, lower and upper case letters and special characters")
    private String password;
    @NotNull
    @NotEmpty
    private String fullName;
    private String phoneNumber;
    @Email
    private String mail;
    @Size(min = 5, max = 5, message = "Postal Code has 5 numbers.")
    private String postalCode;

    public User toUser(PasswordEncoder passwordEncoder, String role) {
        return new User(this.username, passwordEncoder.encode(this.password), this.fullName, this.phoneNumber, this.postalCode, role, this.mail);
    }
}
