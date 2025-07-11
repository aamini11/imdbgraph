import { Ratings } from "@/lib/data/types";

export const allShows = [
  {
    endYear: "2008",
    imdbId: "tt0417299",
    numVotes: 193629,
    rating: 9.2,
    startYear: "2005",
    title: "Avatar: The Last Airbender",
  },
  {
    endYear: "2019",
    imdbId: "tt0944947",
    numVotes: 1563413,
    rating: 9.4,
    startYear: "2011",
    title: "Game of Thrones",
  },
  {
    endYear: null,
    imdbId: "tt0096697",
    numVotes: 4,
    rating: 9,
    startYear: "1989",
    title: "The Simpsons",
  },
];

export const allEpisodes = [
  {
    episodeId: "tt0772328",
    episodeNum: 2,
    numVotes: 1705,
    rating: 8.4,
    seasonNum: 1,
    showId: "tt0417299",
    title: "The Avatar Returns",
  },
  {
    episodeId: "tt5827942",
    episodeNum: 0,
    numVotes: 0,
    rating: 0,
    seasonNum: 1,
    showId: "tt0417299",
    title: "Avatar: The Last Airbender",
  },
  {
    episodeId: "tt0801470",
    episodeNum: 1,
    numVotes: 1953,
    rating: 8.2,
    seasonNum: 1,
    showId: "tt0417299",
    title: "The Boy in the Iceberg",
  },
  {
    episodeId: "tt0762600",
    episodeNum: 1,
    numVotes: 0,
    rating: 0,
    seasonNum: 2,
    showId: "tt0417299",
    title: "The Avatar State",
  },
  {
    episodeId: "tt1480055",
    episodeNum: 1,
    numVotes: 36939,
    rating: 9.1,
    seasonNum: 1,
    showId: "tt0944947",
    title: "Winter Is Coming",
  },
  {
    episodeId: "tt1668746",
    episodeNum: 2,
    numVotes: 27976,
    rating: 8.8,
    seasonNum: 1,
    showId: "tt0944947",
    title: "The Kingsroad",
  },
  {
    episodeId: "tt1829962",
    episodeNum: 3,
    numVotes: 26458,
    rating: 8.7,
    seasonNum: 1,
    showId: "tt0944947",
    title: "Lord Snow",
  },
  {
    episodeId: "tt1971833",
    episodeNum: 1,
    numVotes: 23735,
    rating: 8.9,
    seasonNum: 2,
    showId: "tt0944947",
    title: "The North Remembers",
  },
  {
    episodeId: "tt2069318",
    episodeNum: 2,
    numVotes: 22413,
    rating: 8.6,
    seasonNum: 2,
    showId: "tt0944947",
    title: "The Night Lands",
  },
];

export const avatarRatings: Ratings = {
  show: {
    imdbId: "tt0417299",
    title: "Avatar: The Last Airbender",
    startYear: "2005",
    endYear: "2008",
    rating: 9.2,
    numVotes: 193629,
  },
  allEpisodeRatings: {
    "1": {
      "0": {
        title: "Avatar: The Last Airbender",
        seasonNum: 1,
        episodeNum: 0,
        rating: 0,
        numVotes: 0,
      },
      "1": {
        title: "The Boy in the Iceberg",
        seasonNum: 1,
        episodeNum: 1,
        rating: 8.2,
        numVotes: 1953,
      },
      "2": {
        title: "The Avatar Returns",
        seasonNum: 1,
        episodeNum: 2,
        rating: 8.4,
        numVotes: 1705,
      },
    },
    "2": {
      "1": {
        title: "The Avatar State",
        seasonNum: 2,
        episodeNum: 1,
        rating: 0,
        numVotes: 0,
      },
    },
  },
};

export const gameOfThronesRatings: Ratings = {
  allEpisodeRatings: {
    "1": {
      "1": {
        episodeNum: 1,
        numVotes: 36939,
        rating: 9.1,
        seasonNum: 1,
        title: "Winter Is Coming",
      },
      "2": {
        episodeNum: 2,
        numVotes: 27976,
        rating: 8.8,
        seasonNum: 1,
        title: "The Kingsroad",
      },
      "3": {
        episodeNum: 3,
        numVotes: 26458,
        rating: 8.7,
        seasonNum: 1,
        title: "Lord Snow",
      },
    },
    "2": {
      "1": {
        episodeNum: 1,
        numVotes: 23735,
        rating: 8.9,
        seasonNum: 2,
        title: "The North Remembers",
      },
      "2": {
        episodeNum: 2,
        numVotes: 22413,
        rating: 8.6,
        seasonNum: 2,
        title: "The Night Lands",
      },
    },
  },
  show: {
    endYear: "2019",
    imdbId: "tt0944947",
    numVotes: 1563413,
    rating: 9.4,
    startYear: "2011",
    title: "Game of Thrones",
  },
};

export const simpsonsRatings: Ratings = {
  show: {
    imdbId: "tt0096697",
    title: "The Simpsons",
    startYear: "1989",
    endYear: null,
    rating: 9.0,
    numVotes: 4,
  },
  allEpisodeRatings: {},
};
