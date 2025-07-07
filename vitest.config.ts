import tsconfigPaths from "vite-tsconfig-paths";
import { defineConfig } from "vitest/config";

export default defineConfig({
  plugins: [tsconfigPaths()],
  test: {
    coverage: {
      provider: "v8",
      reporter: ["text", "lcov", "clover"],
      include: ["src/**/*.{ts,tsx}"],
    },
    projects: [
      {
        extends: true,
        test: {
          name: "unit",
          include: ["src/**/*.unit.test.{ts,tsx}"],
          environment: "jsdom",
        },
      },
      {
        extends: true,
        test: {
          name: "db",
          include: ["src/**/*.db.test.{ts,tsx}"],
          globalSetup: ["tests/utils/db-setup.ts"],
          testTimeout: 30000, // Extra time for slower database tests
          environment: "node",
          env: {
            // This password is NOT sensitive. Only used to connect to local db.
            DATABASE_URL:
              "postgresql://localhost:5432/postgres?user=postgres&password=postgres",
          },
        },
      },
    ],
  },
});
