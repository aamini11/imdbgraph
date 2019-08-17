create schema imdb;

alter schema imdb owner to aamini;

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

create table if not exists episode
(
    show_id varchar(10) not null
        constraint episode_title_imdb_id_fk
            references title,
    episode_id varchar(10) not null
        constraint episode_pk
            primary key,
    season_num integer,
    episode_num integer
);

alter table episode owner to aamini;

create index if not exists episode_show_id_index
    on episode (show_id);

create table if not exists rating
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

create index if not exists title_title_type_index
    on title (title_type)
    where (title_type = 'tvSeries'::text);

create index if not exists title_primary_title_index
    on title (to_tsvector('english', primary_title) desc);

