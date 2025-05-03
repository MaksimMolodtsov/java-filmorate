CREATE TABLE IF NOT EXISTS friends (
    user_id int,
    friend_id int,
    CONSTRAINT friends_pk PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS users (
    user_id int NOT NULL AUTO_INCREMENT,
    email varchar NOT NULL,
    login varchar NOT NULL,
    name varchar,
    birthday date,
    CONSTRAINT users_pk PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS likes (
    user_id int,
    film_id int,
    CONSTRAINT likes_pk PRIMARY KEY (user_id, film_id)
);

CREATE TABLE IF NOT EXISTS films (
    film_id int NOT NULL AUTO_INCREMENT,
    name varchar NOT NULL,
    description varchar(200),
    release_date date,
    duration int,
    rating_id int,
    CONSTRAINT films_pk PRIMARY KEY (film_id)
);

CREATE TABLE IF NOT EXISTS mpa_rating (
    rating_id int NOT NULL AUTO_INCREMENT,
    rating varchar NOT NULL,
    CONSTRAINT rating_pk PRIMARY KEY (rating_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id int,
    genre_id int,
    CONSTRAINT film_genres_pk PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id int NOT NULL AUTO_INCREMENT,
    title varchar,
    CONSTRAINT genres_pk PRIMARY KEY (genre_id)
);

