import {
  allEpisodes,
  allShows,
  avatarRatings,
  gameOfThronesRatings,
  simpsonsRatings,
} from "./test-files/expected-data";
import { db } from "@/db/connection";
import { getRatings } from "@/db/data/ratings";
import { update } from "@/db/data/scraper";
import { download, ImdbFile } from "@/db/data/scraper/imdb-file-downloader";
import { episode, show } from "@/db/schema";
import fs from "fs/promises";
import path from "path";
import { expect, test, vi } from "vitest";

vi.mock(import("@/db/data/scraper/imdb-file-downloader"));

// =============================================================================
// Tests
// =============================================================================
test("Loading sample files into database", async () => {
  mockDownloads({
    "title.basics.tsv.gz": "./test-files/titles.tsv",
    "title.episode.tsv.gz": "./test-files/episodes.tsv",
    "title.ratings.tsv.gz": "./test-files/ratings.tsv",
  });

  await update();
  await update();

  expect(await db.select().from(show)).toEqual(allShows);
  expect(await db.select().from(episode)).toEqual(allEpisodes);

  expect(await getRatings("tt0417299")).toEqual(avatarRatings);
  expect(await getRatings("tt0944947")).toEqual(gameOfThronesRatings);
  expect(await getRatings("tt0096697")).toEqual(simpsonsRatings);
});

test("Handling bad files", async () => {
  mockDownloads({
    "title.basics.tsv.gz": "./test-files/titles.tsv",
    "title.episode.tsv.gz": "./test-files/bad-episodes.tsv",
    "title.ratings.tsv.gz": "./test-files/ratings.tsv",
  });

  await expect(update()).rejects.toThrow("Error updating database");
});

// =============================================================================
// Helpers
// =============================================================================
function mockDownloads(mockedFiles: Record<ImdbFile, string>) {
  const fakeDownloadFn = async (imdbFile: ImdbFile, output: string) => {
    const inputFile = path.join(__dirname, mockedFiles[imdbFile]);
    await fs.copyFile(inputFile, output);
  };

  vi.mocked(download).mockImplementation(fakeDownloadFn);
}
