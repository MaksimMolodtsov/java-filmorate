# java-filmorate
Template repository for Filmorate project.

![ER](/ER.png)

```declarative

users
-------
user_id int PK
name varchar
login varchar
birthday date
email varchar

films
-------
film_id int PK
name varchar
description varchar
release_date date
duration int
rating_id int FK >- mpa_rating.rating_id

mpa_rating
-------
rating_id int PK
rating varchar

film_genres
-------
film_id int FK >- films.film_id
genre_id int FK >- genres.genre_id

genres
-------
genre_id int PK
title varchar

likes
-------
film_id int FK >- films.film_id
user_id int FK >- users.user_id

friends
-------
user_id int FK >- users.user_id
friends_id int FK >- users.user_id
friendship boolean