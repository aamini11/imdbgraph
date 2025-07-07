CREATE TABLE "episode" (
	"show_id" varchar(10) NOT NULL,
	"episode_id" varchar(10) PRIMARY KEY NOT NULL,
	"title" text NOT NULL,
	"season_num" integer NOT NULL,
	"episode_num" integer NOT NULL,
	"rating" double precision NOT NULL,
	"num_votes" integer NOT NULL
);
--> statement-breakpoint
CREATE TABLE "show" (
	"imdb_id" varchar(10) PRIMARY KEY NOT NULL,
	"title" text NOT NULL,
	"start_year" char(4) NOT NULL,
	"end_year" char(4),
	"rating" double precision DEFAULT 0 NOT NULL,
	"num_votes" integer DEFAULT 0 NOT NULL
);
--> statement-breakpoint
ALTER TABLE "episode" ADD CONSTRAINT "episode_show_imdb_id_fk" FOREIGN KEY ("show_id") REFERENCES "public"."show"("imdb_id") ON DELETE no action ON UPDATE no action;--> statement-breakpoint
CREATE INDEX "episode_show_id_index" ON "episode" USING btree ("show_id");--> statement-breakpoint
CREATE INDEX "show_rating_index" ON "show" USING btree ("rating" DESC NULLS LAST);--> statement-breakpoint
CREATE EXTENSION pg_trgm;
