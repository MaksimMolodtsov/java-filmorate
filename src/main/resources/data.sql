INSERT INTO genres (genre_id, title)
SELECT 1, 'Комедия' WHERE NOT EXISTS ( SELECT 1 FROM genres WHERE title = 'Комедия' );
INSERT INTO genres (genre_id, title)
SELECT 2, 'Драма' WHERE NOT EXISTS ( SELECT 1 FROM genres WHERE title = 'Драма' );
INSERT INTO genres (genre_id, title)
SELECT 3, 'Мультфильм' WHERE NOT EXISTS ( SELECT 1 FROM genres WHERE title = 'Мультфильм' );
INSERT INTO genres (genre_id, title)
SELECT 4, 'Триллер' WHERE NOT EXISTS ( SELECT 1 FROM genres WHERE title = 'Триллер' );
INSERT INTO genres (genre_id, title)
SELECT 5, 'Документальный' WHERE NOT EXISTS ( SELECT 1 FROM genres WHERE title = 'Документальный' );
INSERT INTO genres (genre_id, title)
SELECT 6, 'Боевик' WHERE NOT EXISTS ( SELECT 1 FROM genres WHERE title = 'Боевик' );

INSERT INTO mpa_rating (rating_id, rating)
SELECT 1, 'G' WHERE NOT EXISTS ( SELECT 1 FROM mpa_rating WHERE rating = 'G' );
INSERT INTO mpa_rating (rating_id, rating)
SELECT 2, 'PG' WHERE NOT EXISTS ( SELECT 1 FROM mpa_rating WHERE rating = 'PG' );
INSERT INTO mpa_rating (rating_id, rating)
SELECT 3, 'PG-13' WHERE NOT EXISTS ( SELECT 1 FROM mpa_rating WHERE rating = 'PG-13' );
INSERT INTO mpa_rating (rating_id, rating)
SELECT 4, 'R' WHERE NOT EXISTS ( SELECT 1 FROM mpa_rating WHERE rating = 'R' );
INSERT INTO mpa_rating (rating_id, rating)
SELECT 5, 'NC-17' WHERE NOT EXISTS ( SELECT 1 FROM mpa_rating WHERE rating = 'NC-17' );