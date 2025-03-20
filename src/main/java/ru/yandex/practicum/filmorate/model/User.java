package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {

    private Long id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String login;
    private String name;
    private LocalDate birthday;

    @Builder
    public User(Long id, LocalDate birthday, String name, String login, String email) {
        this.id = id;
        this.birthday = birthday;
        this.name = name;
        this.login = login;
        this.email = email;
    }

}
