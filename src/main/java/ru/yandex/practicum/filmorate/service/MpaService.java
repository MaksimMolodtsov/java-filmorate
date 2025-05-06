package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.dal.MpaDbStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public Collection<Mpa> allMpa() {
        return mpaDbStorage.allMpa();
    }

    public Mpa getMpaById(Long id) {
        if (id == null) {
            log.warn("Ошибка валидации: Id должен быть указан для необходимого рейтинга");
            throw new ValidationException("Id должен быть указан");
        }
        return mpaDbStorage.getMpaById(id);
    }
}
