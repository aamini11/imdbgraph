import { db } from "@/db/connection";
import fs from "fs/promises";
import path from "path";
import { beforeEach } from "vitest";

beforeEach(async () => {
  await wipeDb();
  await setUpSchema();
});

/**
 * Run SQL schema migration files.
 */
async function setUpSchema() {
  const migrations = path.join(process.cwd(), "./drizzle");
  const files = await fs.readdir(migrations);
  const sqlFiles = files.filter((file: string) => file.endsWith(".sql")).sort();
  for (const file of sqlFiles) {
    const content = await fs.readFile(path.join(migrations, file), "utf-8");
    // Split on statement breakpoints and execute each statement
    const statements = content
      .split("--> statement-breakpoint")
      .map((stmt: string) => stmt.trim())
      .filter((stmt: string) => stmt.length > 0);
    for (const statement of statements) {
      await db.execute(statement);
    }
  }
}

async function wipeDb() {
  await db.execute("DROP SCHEMA public CASCADE");
  await db.execute("CREATE SCHEMA public");
}
