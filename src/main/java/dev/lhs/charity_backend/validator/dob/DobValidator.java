package dev.lhs.charity_backend.validator.dob;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class DobValidator implements ConstraintValidator<DobConstraint, LocalDateTime> {

    private int min;

    @Override
    public void initialize(DobConstraint constraintAnnotation) { // lay cai gia tri tu annotation
        ConstraintValidator.super.initialize(constraintAnnotation);
        min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (Objects.isNull(value)) return false;
        long years = ChronoUnit.YEARS.between(value, LocalDateTime.now());
        return years >= min;
    }
}
