"use server";

import { show } from "@/db/schema";
import { Show } from "@/lib/data/types";
import { desc, sql } from "drizzle-orm";
import { NodePgDatabase } from "drizzle-orm/node-postgres";

export async function fetchSuggestions(
  db: NodePgDatabase,
  query: string,
): Promise<Show[] | null> {
  if (!query) {
    return null;
  }
  return await db
    .select()
    .from(show)
    .where(sql`${query}::text <% title::text`)
    .orderBy(desc(show.numVotes))
    .limit(5);
}
