import {
  avatarRatings,
  gameOfThronesRatings,
  simpsonsRatings,
} from "./test-files/ratings";
import { download, ImdbFile } from "@/lib/data/imdb-file-downloader";
import { getRatings } from "@/lib/data/ratings";
import { update } from "@/lib/data/scraper";
import fs from "fs/promises";
import path from "path";
import { expect, test, vi } from "vitest";

vi.mock("@/lib/data/imdb-file-downloader");

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
