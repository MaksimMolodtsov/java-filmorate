package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmTest {

    private static final Validator validator;

    static {
        try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
            validator = validatorFactory.usingContext().getValidator();
        }
    }

    @Test
    void shouldNotValidateBlankName() {
        Film film = new Film();
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(NotNull.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("mpa", violation.getPropertyPath().toString());
    }

    @Test
    void shouldNotValidateDescriptionMoreThan200Symbols() {
        Film film =  new Film();
        film.setDescription("1".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(Size.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    void shouldValidateDescription() {
        Film film =  new Film();
        film.setDescription("1".repeat(200));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidateReleaseDate() {
        Film film = new Film();
        film.setReleaseDate(LocalDate.of(1895, 12, 29));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        System.out.println(violations.toString());
        assertFalse(violations.isEmpty());
    }

    @Test
    void shouldValidate() {
        Film film =  new Film();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }
}