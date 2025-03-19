package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserTest {
    private static final Validator validator;

    static {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    static User getUser() {
        return User.builder()
                .id(1L)
                .login("Логин")
                .email("abc@cba.com")
                .name("Имя")
                .birthday(LocalDate.of(2000, 5, 5))
                .build();
    }

    @Test
    void shouldValidate() {
        User user = getUser();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldNotValidateEmailWithoutDog() {
        User user = getUser();
        user.setEmail("lll.5785");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals(Email.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    void shouldNotValidateBlankEmail() {
        User user = getUser();
        user.setEmail("");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals(NotBlank.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    void shouldNotValidateEmailWithMisplacedAt() {
        User user = getUser();
        user.setEmail("@lll.2658");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        ConstraintViolation<User> violation = violations.iterator().next();
        assertEquals(Email.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("email", violation.getPropertyPath().toString());
    }

    @Test
    void shouldNotValidateBlankLogin() {
        User user = getUser();
        user.setLogin("");


        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

}