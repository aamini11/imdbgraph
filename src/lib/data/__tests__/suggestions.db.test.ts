import { fetchSuggestions } from "@/lib/data/suggestions";
import { testWithStagingDb } from "@/tests/utils/db-test-fixture";
import { describe, expect } from "vitest";

// =============================================================================
// Tests
// =============================================================================
describe("Test Search functionality", () => {
  testWithStagingDb("Searching: Avatar", async ({ db }) => {
    const results = await fetchSuggestions(db, "Av");
    expect(results?.map((show) => show.title)).toEqual([
      "Avatar: The Last Airbender",
      "Avatar: The Last Airbender",
      "Avenue 5",
      "Avrupa Yakasi",
      "What If... Captain Carter Were the First Avenger?",
    ]);
  });

  testWithStagingDb("Searching: King of The Hill", async ({ db }) => {
    const results = await fetchSuggestions(db, "King");
    expect(results?.map((show) => show.title)).toEqual([
      "Breaking Bad",
      "The Walking Dead",
      "The Last Kingdom",
      "Fear the Walking Dead",
      "A Knight of the Seven Kingdoms",
    ]);
  });
});
