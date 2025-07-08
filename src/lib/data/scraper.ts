import { db } from "@/db/connection";
import { download } from "@/lib/data/imdb-file-downloader";
import { randomUUID } from "node:crypto";
import { createReadStream } from "node:fs";
import { mkdir } from "node:fs/promises";
import { tmpdir } from "node:os";
import path from "node:path";
import { pipeline } from "node:stream/promises";
import { PoolClient } from "pg";
import { from as copyFrom } from "pg-copy-streams";

/**
 * Main method that downloads the latest files from IMDB and updates our
 * internal database with the latest data.
 */
export async function update(): Promise<void> {
  const client = await db.$client.connect();
  try {
    await client.query("BEGIN");
    await transfer(client);
    await client.query("COMMIT");
  } catch (error) {
    await client.query("ROLLBACK");
    console.error(error);
    throw new Error("Error updating database", { cause: error });
  } finally {
    client.release();
  }
}

/**
 * Download files and store them in temp tables using efficient bulk operations.
 * Once all data is loaded into temp tables, update all the real tables with
 * data from the temp tables.
 */
async function transfer(client: PoolClient) {
  await client.query(`
    CREATE TEMPORARY TABLE temp_title
    (
        imdb_id         VARCHAR(10),
        title_type      TEXT,
        primary_title   TEXT,
        original_title  TEXT,
        is_adult        BOOLEAN,
        start_year      CHAR(4),
        end_year        CHAR(4),
        runtime_minutes INT,
        genres          TEXT
    ) ON COMMIT DROP;
    
    CREATE TEMPORARY TABLE temp_episode
    (
        episode_id  VARCHAR(10),
        show_id     VARCHAR(10),
        season_num  INT,
        episode_num INT
    ) ON COMMIT DROP;
    
    CREATE TEMPORARY TABLE temp_ratings
    (
        imdb_id     VARCHAR(10) PRIMARY KEY,
        imdb_rating DOUBLE PRECISION,
        num_votes   INT
    ) ON COMMIT DROP;
  `);

  // Download files and store them in temp tables.
  const tempDir = path.join(tmpdir(), `imdb-run-${randomUUID()}`);
  await mkdir(tempDir);
  console.log("Starting downloads...");
  await download("title.basics.tsv.gz", path.join(tempDir, "titles.tsv"));
  await download("title.episode.tsv.gz", path.join(tempDir, "episodes.tsv"));
  await download("title.ratings.tsv.gz", path.join(tempDir, "ratings.tsv"));

  const copy = async (file: string, cmd: string) => {
    const sourceStream = createReadStream(file);
    const ingestStream = client.query(copyFrom(cmd));
    await pipeline(sourceStream, ingestStream);
    console.log(`Successfully transferred ${file} to table temp_title`);
  };

  console.log("Starting file to temp table transfers...");
  await copy(
    path.join(tempDir, "titles.tsv"),
    `COPY temp_title FROM STDIN WITH (DELIMITER '\t', HEADER TRUE) 
    WHERE title_type IN ('tvSeries', 'tvEpisode', 'tvShort', 'tvSpecial', 'tvMiniSeries') AND start_year IS NOT NULL;`,
  );
  await copy(
    path.join(tempDir, "episodes.tsv"),
    "COPY temp_episode FROM STDIN WITH (DELIMITER '\t', HEADER TRUE);",
  );
  await copy(
    path.join(tempDir, "ratings.tsv"),
    "COPY temp_ratings FROM STDIN WITH (DELIMITER '\t', HEADER TRUE);",
  );

  // Update show table using new data from temp tables
  await client.query(`
    INSERT INTO show(imdb_id, title, start_year, end_year, rating, num_votes)
    SELECT imdb_id,
           primary_title,
           start_year,
           end_year,
           COALESCE(imdb_rating, 0.0),
           COALESCE(num_votes, 0)
    FROM temp_title LEFT JOIN temp_ratings USING (imdb_id)
    WHERE num_votes > 0 AND title_type IN ('tvSeries', 'tvMiniSeries')
    ON CONFLICT (imdb_id) DO UPDATE
        SET title = excluded.title,
            start_year = excluded.start_year,
            end_year = excluded.end_year,
            rating = excluded.rating,
            num_votes = excluded.num_votes;
  `);
  console.log("Shows successfully updated");

  // Update episode table using new data from temp tables
  await client.query(`
    INSERT INTO episode(show_id, episode_id, title, season_num, episode_num, rating, num_votes)
    SELECT e.show_id,
           e.episode_id,
           t.primary_title as title,
           e.season_num,
           e.episode_num,
           COALESCE(r.imdb_rating, 0.0) as rating,
           COALESCE(r.num_votes, 0) as num_votes
    FROM temp_episode e
    LEFT JOIN temp_title t ON (e.episode_id = t.imdb_id)
    LEFT JOIN temp_ratings r ON (e.episode_id = r.imdb_id)
    WHERE t.title_type = 'tvEpisode'
    AND e.show_id IN (
      SELECT imdb_id FROM show
    )
    AND e.season_num >= 0
    AND e.episode_num >= 0;
  `);
  console.log("Episodes successfully updated");
}
