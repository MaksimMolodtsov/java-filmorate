package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.serializer.DurationSerializer;

import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {

    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    private LocalDate releaseDate;
    @JsonSerialize(using = DurationSerializer.class)
    private Duration duration;

    @Builder
    public Film(Long id, Duration duration, LocalDate releaseDate, String description, String name) {
        this.id = id;
        this.duration = duration;
        this.releaseDate = releaseDate;
        this.description = description;
        this.name = name;
    }

}
