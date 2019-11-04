CREATE EXTENSION pg_trgm;

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

CREATE INDEX trigram_index ON imdb.show USING GIN (primary_title gin_trgm_ops);

CREATE MATERIALIZED VIEW imdb.valid_show AS
SELECT DISTINCT show_id
FROM imdb.episode
WHERE num_votes > 0;