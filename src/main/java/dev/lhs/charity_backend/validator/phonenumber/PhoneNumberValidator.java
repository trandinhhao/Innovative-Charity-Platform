package dev.lhs.charity_backend.validator.phonenumber;

import dev.lhs.charity_backend.validator.dob.DobConstraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumberConstraint, String> {

    private int length;

    @Override
    public void initialize(PhoneNumberConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
        length = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) return false;
        return value.length() == length && value.matches("^0\\d{9}$");
    }
}
