import { sql } from "drizzle-orm";
import {
  pgTable,
  index,
  integer,
  varchar,
  text,
  char,
  doublePrecision,
  foreignKey,
} from "drizzle-orm/pg-core";

export const show = pgTable(
  "show",
  {
    imdbId: varchar("imdb_id", { length: 10 }).primaryKey().notNull(),
    title: text("title").notNull(),
    startYear: char("start_year", { length: 4 }).notNull(),
    endYear: char("end_year", { length: 4 }),
    rating: doublePrecision("rating").default(0).notNull(),
    numVotes: integer("num_votes").default(0).notNull(),
  },
  (table) => [
    index("show_rating_index").using("btree", table.rating.desc()),
    index("trigram_index").using("gin", sql`${table.title} gin_trgm_ops`),
  ],
);

export const episode = pgTable(
  "episode",
  {
    showId: varchar("show_id", { length: 10 }).notNull(),
    episodeId: varchar("episode_id", { length: 10 }).primaryKey().notNull(),
    title: text("title").notNull(),
    seasonNum: integer("season_num").notNull(),
    episodeNum: integer("episode_num").notNull(),
    rating: doublePrecision("rating").notNull(),
    numVotes: integer("num_votes").notNull(),
  },
  (table) => [
    index("episode_show_id_index").using("btree", table.showId),
    foreignKey({
      columns: [table.showId],
      foreignColumns: [show.imdbId],
      name: "episode_show_imdb_id_fk",
    }),
  ],
);
