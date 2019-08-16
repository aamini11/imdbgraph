create schema imdb;

alter schema imdb owner to aamini;

create table title
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

create table episode
(
    show_id varchar(10) not null
        constraint episode_title_imdb_id_fk
            references title,
    episode_id varchar(10) not null
        constraint episode_pk
            primary key,
    season integer,
    episode integer
);

alter table episode owner to aamini;

create table rating
(
    imdb_id varchar(10) not null
        constraint rating_pk
            primary key
        constraint rating_title_imdb_id_fk
            references title,
    imdb_rating double precision,
    num_votes integer
);

alter table rating owner to aamini;

create index title_title_type_index
    on title (title_type)
    where (title_type = 'tvSeries'::text);

create index title_primary_title_index
    on title (to_tsvector('english', primary_title));