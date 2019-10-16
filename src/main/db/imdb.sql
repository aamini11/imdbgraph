CREATE TABLE imdb.rateable_title
(
    imdb_id     VARCHAR(10) PRIMARY KEY,
    imdb_rating DOUBLE PRECISION DEFAULT 0.0,
    num_votes   INTEGER          DEFAULT 0
);

CREATE TABLE imdb.show
(
    imdb_id       VARCHAR(10) PRIMARY KEY REFERENCES imdb.rateable_title,
    primary_title TEXT,
    start_year    CHAR(4),
    end_year      CHAR(4)
);

CREATE TABLE imdb.episode
(
    show_id       VARCHAR(10) REFERENCES imdb.show,
    episode_id    VARCHAR(10) PRIMARY KEY REFERENCES imdb.rateable_title,
    episode_title TEXT,
    season_num    INTEGER,
    episode_num   INTEGER
);

CREATE INDEX episode_show_id_index
    ON imdb.episode (show_id);

CREATE INDEX title_primary_title_index
    ON imdb.show USING gin (to_tsvector('english', primary_title));

CREATE MATERIALIZED VIEW imdb.ratings_count AS
SELECT show_id, SUM(rateable_title.num_votes)
FROM imdb.episode
         JOIN imdb.rateable_title ON (episode_id = rateable_title.imdb_id)
GROUP BY show_id
HAVING SUM(rateable_title.num_votes) > 0