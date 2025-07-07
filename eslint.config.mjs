import { FlatCompat } from "@eslint/eslintrc";
import js from "@eslint/js";
import tanstack from "@tanstack/eslint-plugin-query";
import prettier from "eslint-config-prettier/flat";
import { defineConfig, globalIgnores } from "eslint/config";
import globals from "globals";
import tseslint from "typescript-eslint";

const compat = new FlatCompat({
  baseDirectory: import.meta.dirname,
});

const noRelativeImportsRule = {
  ignores: ["**/*.test.*", "**/__tests__/**"],
  rules: {
    "no-restricted-imports": [
      "error",
      {
        patterns: [
          {
            group: ["./", "../", "!./global.css"],
            message:
              "Please use absolute imports with '@/'. Example: '@/lib/utils'.",
          },
        ],
      },
    ],
  },
};

/** @type {import("eslint").Linter.Config[]} */
export let config = [
  globalIgnores(["node_modules", ".next"]),
  {
    files: ["**/*.{js,mjs,cjs,ts,mts,cts,jsx,tsx}"],
    plugins: { js },
    extends: ["js/recommended"],
    languageOptions: { globals: { ...globals.browser, ...globals.node } },
  },
  {
    files: ["**/*.{ts,tsx}"],
    extends: [
      tseslint.configs.strictTypeChecked,
      tseslint.configs.stylisticTypeChecked,
    ],
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },
  {
    files: ["**/*.{js,mjs,cjs,jsx}"],
    extends: [tseslint.configs.disableTypeChecked],
  },
  ...compat.config({
    extends: ["next/core-web-vitals"],
  }),
  noRelativeImportsRule,
  ...tanstack.configs["flat/recommended"],
  prettier,
];

export default defineConfig(config);
