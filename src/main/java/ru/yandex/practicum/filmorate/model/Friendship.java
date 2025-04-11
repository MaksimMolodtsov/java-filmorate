package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Friendship {

    @NotNull
    final Long userId;
    @NotNull
    final Long friendId;
    final boolean isFriend;
}