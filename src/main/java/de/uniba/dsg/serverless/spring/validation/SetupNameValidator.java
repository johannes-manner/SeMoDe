package de.uniba.dsg.serverless.spring.validation;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Value;

public class SetupNameValidator implements ConstraintValidator<SetupNameConstraint, String> {

    @Value("${semode.setups.path}")
    private String setups;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isEmpty()) {
            return false;
        }

        // check if setup name is unique
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(this.setups))) {
            for (Path path : stream) {
                if (path.getFileName().toString().equals(value)) {
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
