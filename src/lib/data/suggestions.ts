"use server";

import { db } from "@/db/connection";
import { show } from "@/db/schema";
import { Show } from "@/lib/data/types";
import { desc, sql } from "drizzle-orm";

export async function fetchSuggestions(query: string): Promise<Show[] | null> {
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
