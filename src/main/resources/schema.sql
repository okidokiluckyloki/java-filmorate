
DROP TABLE IF EXISTS
users, follows, ratings,
genres, films, likes,
genres_films, directors, directors_films, reviews, reviews_likes CASCADE;

create table if not exists users (
id integer generated by default as identity not null primary key,
name varchar(255),
email varchar(255) not null,
login varchar(255) not null,
birthday date
);

CREATE TABLE if not exists follows (
    following_id integer not null references users(id) on delete cascade,
    followed_id integer not null references users(id) on delete cascade
);

create table if not exists ratings (
id integer generated by default as identity not null primary key,
name varchar(200) not null unique
);

create table if not exists genres (
id integer generated by default as identity not null primary key,
name varchar (200) not null unique
);

create table if not exists films (
id integer generated by default as identity not null primary key,
name varchar(200),
description varchar(200),
release date,
duration integer not null,
rating_id integer, foreign key (rating_id) references ratings(id)
);

CREATE TABLE if not exists likes (
  film_id integer not null references films(id) on delete cascade,
  user_id integer not null references users(id) on delete cascade
);

CREATE TABLE if not exists genres_films (
    genre_id integer not null references genres(id) on delete cascade,
    film_id integer not null references films(id) on delete cascade
);

create table if not exists directors (
id integer generated by default as identity not null primary key,
name varchar(200) not null
);

create table if not exists directors_films (
director_id integer not null references directors(id) on delete cascade,
film_id integer not null references films(id) on delete cascade
);

create table if not exists reviews (
id int generated by default as identity not null primary key,
content varchar(500) not null,
is_positive boolean,
user_id int not null references users(id) on delete cascade,
film_id int not null references films(id) on delete cascade,
useful int default 0
);

create table if not exists reviews_likes (
review_id integer not null references reviews(id) on delete cascade,
user_id integer not null references users(id) on delete cascade,
review_like integer
);
