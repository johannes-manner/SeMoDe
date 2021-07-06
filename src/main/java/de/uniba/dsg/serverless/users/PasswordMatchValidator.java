package de.uniba.dsg.serverless.users;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, RegistrationForm> {

    private String fieldName;

    @Override
    public void initialize(final PasswordMatch p) {
        this.fieldName = p.fieldName();
    }

    @Override
    public boolean isValid(RegistrationForm value, ConstraintValidatorContext context) {
        boolean isValid = false;
        if (value != null && value.getPassword() != null) {
            isValid = value.getPassword().equals(value.getRepeatPassword());
        }

        // Overriding the default constraint violation behavior here to show the error message on the field.
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate()).addPropertyNode(fieldName).addConstraintViolation();
        }

        return isValid;
    }
}
