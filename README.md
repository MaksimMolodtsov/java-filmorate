# java-filmorate
Template repository for Filmorate project.

![ER](/ER.png)

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
mpa_rating_id int FK >- mpa_rating.mpa_rating_id

mpa_rating
-------
mpa_rating_id int PK
mpa_rating varchar

genres
-------
film_id int FK >- films.film_id
genre_id int PK
title varchar

likes
-------
id int PK
film_id int FK >- films.film_id
user_id int FK >- users.user_id

friends
-------
couple_id int PK
user_id int FK >- users.user_id
friends_id int FK >- users.user_id