create schema imdb;

alter schema imdb owner to aamini;

create table if not exists episode
(
    show_id varchar(10) not null,
    episode_id varchar(10) not null,
    season integer,
    episode integer,
    constraint episode_pk
        primary key (show_id, episode_id)
);

alter table episode owner to aamini;

create table if not exists rating
(
    imdb_id varchar(10) not null
        constraint rating_pk
            primary key,
    imdb_rating double precision,
    num_votes integer
);

alter table rating owner to aamini;

create table if not exists title
(
    imdb_id varchar(10) not null
        constraint title_pk
            primary key,
    primary_title text,
    title_type text,
    start_year char(4),
    end_year char(4)
);

alter table title owner to aamini;

create index if not exists title_title_type_index
    on title (title_type);

