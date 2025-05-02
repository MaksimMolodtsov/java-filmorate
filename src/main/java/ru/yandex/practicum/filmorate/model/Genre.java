package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Genre {

    @NotNull
    private Long id;
    @NotNull
    private String title;
}