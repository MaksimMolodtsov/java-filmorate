package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
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

    static Film getFilm() {
        return Film.builder()
                .id(1L)
                .name("Тест")
                .duration(Duration.ofSeconds(100))
                .releaseDate(LocalDate.now())
                .description("Описание")
                .build();
    }

    @Test
    void shouldNotValidateBlankName() {
        Film film = getFilm();
        film.setName("");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(NotBlank.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("name", violation.getPropertyPath().toString());
    }

    @Test
    void shouldNotValidateDescriptionMoreThan200Symbols() {
        Film film = getFilm();
        film.setDescription("1".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
        ConstraintViolation<Film> violation = violations.iterator().next();
        assertEquals(Size.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("description", violation.getPropertyPath().toString());
    }

    @Test
    void shouldValidateDescription() {
        Film film = getFilm();
        film.setDescription("1".repeat(200));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldValidateReleaseDate() {
        Film film = getFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 29));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        System.out.println(violations.toString());
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldValidate() {
        Film film = getFilm();
        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty());
    }
}