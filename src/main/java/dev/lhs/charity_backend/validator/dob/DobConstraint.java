package dev.lhs.charity_backend.validator.dob;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD}) // chi ap dung cho field cua class
@Retention(RUNTIME) // giu lai trong luc chuong trinh chay
@Constraint(validatedBy = {DobValidator.class})
public @interface DobConstraint { // custom annotation

    String message() default "Invalid date of birth";
    int min();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
