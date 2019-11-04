CREATE TABLE imdb.show
(
    imdb_id       VARCHAR(10) PRIMARY KEY,
    primary_title TEXT,
    start_year    CHAR(4),
    end_year      CHAR(4),
    imdb_rating   DOUBLE PRECISION DEFAULT 0.0,
    num_votes     INTEGER          DEFAULT 0
);

CREATE TABLE imdb.episode
(
    show_id       VARCHAR(10) REFERENCES imdb.show,
    episode_id    VARCHAR(10) PRIMARY KEY,
    episode_title TEXT,
    season_num    INTEGER,
    episode_num   INTEGER,
    imdb_rating   DOUBLE PRECISION DEFAULT 0.0,
    num_votes     INTEGER          DEFAULT 0
);

CREATE INDEX episode_show_id_index
    ON imdb.episode (show_id);

CREATE INDEX title_primary_title_index
    ON imdb.show USING gin (to_tsvector('english', primary_title));