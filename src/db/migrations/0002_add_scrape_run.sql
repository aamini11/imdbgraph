CREATE TABLE "scrape_run" (
	"id" serial PRIMARY KEY NOT NULL,
	"completed_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
