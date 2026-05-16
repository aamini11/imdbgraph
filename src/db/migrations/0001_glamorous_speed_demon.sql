CREATE EXTENSION IF NOT EXISTS "pg_trgm";--> statement-breakpoint
DROP INDEX IF EXISTS "trigram_index";--> statement-breakpoint
CREATE INDEX IF NOT EXISTS "show_title_trigram_index" ON "show" USING gin (title gin_trgm_ops);
