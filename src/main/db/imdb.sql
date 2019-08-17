create table if not exists imdb.title
(
    imdb_id varchar(10) not null
        constraint title_pk
            primary key,
    primary_title text,
    title_type text,
    start_year char(4),
    end_year char(4)
);

create table if not exists imdb.episode
(
    show_id varchar(10) not null
        constraint episode_title_imdb_id_fk
            references imdb.title,
    episode_id varchar(10) not null
        constraint episode_pk
            primary key,
    season_num integer,
    episode_num integer
);

create index if not exists episode_show_id_index
    on imdb.episode (show_id);

create table if not exists imdb.rating
(
    imdb_id varchar(10) not null
        constraint rating_pk
            primary key
        constraint rating_title_imdb_id_fk
            references imdb.title,
    imdb_rating double precision,
    num_votes integer
);

create index if not exists rating_num_votes_index
    on imdb.rating (num_votes desc);

create index if not exists title_title_type_index
    on imdb.title (title_type)
    where (title_type = 'tvSeries'::text OR title_type = 'tvEpisode');

create index if not exists title_primary_title_index
    on imdb.title (to_tsvector('english', primary_title));