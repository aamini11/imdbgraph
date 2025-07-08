import { loadEnvConfig } from "@next/env";
import tsconfigPaths from "vite-tsconfig-paths";
import { defineConfig } from "vitest/config";

loadEnvConfig(process.cwd());

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
          setupFiles: ["tests/utils/db-setup.ts"],
          testTimeout: 30000, // Extra time for slower database tests
          environment: "node",
        },
      },
    ],
  },
});
