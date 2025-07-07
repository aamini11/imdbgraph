/**
 * Standalone script to populate the database with IMDB data Usage: pnpm run
 * populate
 */
import { db } from "@/db/connection";
import { update } from "@/lib/data/scraper";

async function main() {
  console.log("Starting database population...");

  try {
    const startTime = Date.now();
    await update(db.$client);
    const endTime = Date.now();
    const duration = ((endTime - startTime) / 1000).toFixed(2);

    console.log(
      `✅ Database population completed successfully in ${duration} seconds!`,
    );
    process.exit(0);
  } catch (error) {
    console.error("❌ Database population failed:");
    console.error(error);
    process.exit(1);
  }
}

void main();
