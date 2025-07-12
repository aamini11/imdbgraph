/**
 * Standalone script to populate the database with IMDB data Usage: pnpm run
 * populate
 */
import { update } from "@/db/data/scraper";
import { loadEnvConfig } from "@next/env";

// Load variables from .env.test
loadEnvConfig(process.cwd());

async function main() {
  console.log("Starting database population...");

  try {
    const startTime = Date.now();
    await update();
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
