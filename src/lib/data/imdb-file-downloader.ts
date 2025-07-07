import { createWriteStream } from "fs";
import { Readable } from "node:stream";
import { pipeline } from "node:stream/promises";
import { ReadableStream } from "node:stream/web";
import { createGunzip } from "zlib";

// https://www.imdb.com/interfaces
const baseUri = "https://datasets.imdbws.com";
export type ImdbFile =
  | "title.basics.tsv.gz"
  | "title.episode.tsv.gz"
  | "title.ratings.tsv.gz";

export async function download(file: ImdbFile, output: string): Promise<void> {
  const uri = `${baseUri}/${file}`;
  try {
    const { body, ok, status } = await fetch(uri);
    if (!ok) {
      throw new Error(`HTTP error! status: ${status.toString()}`);
    }
    if (!body) {
      throw new Error("Response body is null");
    }
    await pipeline(
      Readable.fromWeb(body as ReadableStream),
      createGunzip(),
      createWriteStream(output),
    );
    console.log(`Download completed: ${output.toString()}`);
  } catch (error) {
    throw new Error(`Failed to download ${uri}`, { cause: error });
  }
}
