create table imdb.episode
(
    show_id varchar(10) not null,
    episode_id varchar(10) not null,
    season integer,
    episode integer,
    constraint episode_pk
        primary key (show_id, episode_id)
);

alter table imdb.episode owner to aamini;

create table imdb.rating
(
    imdb_id varchar(10) not null
        constraint rating_pk
            primary key,
    imdb_rating double precision,
    num_votes integer
);

alter table imdb.rating owner to aamini;

create table imdb.title
(
    imdb_id varchar(10) not null
        constraint title_pk
            primary key,
    primary_title text,
    title_type text,
    start_year char(4),
    end_year char(4)
) PARTITION BY LIST (imdb_id);

alter table imdb.title owner to aamini;

create index title_title_type_index
    on imdb.title (title_type) where title_type = 'tvSeries'

