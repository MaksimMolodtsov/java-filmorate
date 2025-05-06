package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private Long id;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String login;
    @NotBlank
    private String name;
    private LocalDate birthday;
    private Set<Long> friends = new HashSet<>();
    private Set<Long> followers = new HashSet<>();

}
