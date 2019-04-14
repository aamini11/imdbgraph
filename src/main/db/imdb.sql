create schema imdb;

alter schema imdb owner to postgres;

create table if not exists episode
(
    show_id varchar(10) not null,
    episode_id varchar(10) not null
        constraint ratings_pk
            primary key,
    season integer not null,
    episode integer not null,
    constraint episode_pk
        unique (episode_id, episode, season)
);

alter table episode owner to postgres;

create table if not exists show_title
(
    show_id varchar(10) not null
        constraint show_pk
            primary key,
    primary_title text,
    start_year char(4),
    end_year char(4)
);

alter table show_title owner to postgres;

create table if not exists episode_title
(
    episode_id varchar(10) not null
        constraint episode_title_pk
            primary key,
    primary_title text
);

alter table episode_title owner to postgres;

create table if not exists rating
(
    imdb_id varchar(10) not null
        constraint rating_pk
            primary key,
    imdb_rating double precision not null,
    num_votes integer not null
);

alter table rating owner to postgres;

