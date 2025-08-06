package dev.lhs.charity_backend.validator.phonenumber;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = {PhoneNumberValidator.class})
public @interface PhoneNumberConstraint {

    String message() default "Invalid phone number";
    int length();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

}
