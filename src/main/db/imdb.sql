create table imdb.episode
(
    show_id    varchar(10) not null
        constraint episode_title_imdb_id_fk
            references imdb.title,
    episode_id varchar(10) not null,
    season     integer,
    episode    integer,
    constraint episode_pk
        primary key (show_id, episode_id)
);

alter table imdb.episode
    owner to aamini;

create table imdb.rating
(
    imdb_id     varchar(10) not null
        constraint rating_pk
            primary key,
    imdb_rating double precision,
    num_votes   integer
);

alter table imdb.rating
    owner to aamini;

create index rating_num_votes_index
    on imdb.rating (num_votes desc);

create table imdb.title
(
    imdb_id       varchar(10) not null
        constraint title_pk
            primary key,
    primary_title text,
    title_type    text,
    start_year    char(4),
    end_year      char(4)
);

alter table imdb.title
    owner to aamini;

CREATE INDEX title_title_type_index ON imdb.title (title_type)
    WHERE title_type = 'tvSeries';

CREATE INDEX title_primary_title_index ON imdb.title USING GIN (to_tsvector('english', primary_title));


