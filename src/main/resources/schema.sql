-- drop tables
DROP TABLE IF EXISTS users, follows, ratings, genres, films, likes, genres_films CASCADE;
-- 1. users
create table if not exists users (
id integer generated by default as identity not null primary key,
name varchar(255),
email varchar(255) not null,
login varchar(255) not null,
birthday date
);
-- 2. follows
create table if not exists follows (
following_id integer not null references users(id),
followed_id integer not null references users(id)
);
-- 3. rating MPA
create table if not exists ratings (
id integer generated by default as identity not null primary key,
name varchar(200) not null unique
);
-- 4. genres
create table if not exists genres (
id integer generated by default as identity not null primary key,
name varchar (200) not null unique
);
-- 5. films
create table if not exists films (
id integer generated by default as identity not null primary key,
name varchar(200),
description varchar(200),
release date,
duration integer not null,
rating_id integer, foreign key (rating_id) references ratings(id)
);
-- 6. likes
create table if not exists likes (
film_id integer not null references films(id),
user_id integer not null references users(id)
);
-- 7 genres_films (
create table if not exists genres_films (
genre_id integer not null references genres(id),
film_id integer not null references films(id)
);