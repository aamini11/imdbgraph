import { fetchSuggestions } from "@/lib/data/suggestions";
import { expect, test } from "vitest";

// =============================================================================
// Tests
// =============================================================================
test.skip("Searching: Avatar", async () => {
  const results = await fetchSuggestions("Av");
  expect(results?.map((show) => show.title)).toEqual([
    "Avatar: The Last Airbender",
    "Avatar: The Last Airbender",
    "Avenue 5",
    "Avrupa Yakasi",
    "Avengers Assemble",
  ]);
});

test.skip("Searching: King of The Hill", async () => {
  const results = await fetchSuggestions("King of");
  expect(results?.map((show) => show.title)).toEqual([
    "King of the Hill",
    "King of the Narrow Sea",
    "King of Kings",
    "King of Mirzapur",
    "King of the Damned",
  ]);
});
