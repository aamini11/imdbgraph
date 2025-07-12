"use client";

import { Theme, useTheme } from "@/components/theme/theme-provider";
import { Episode, Ratings } from "@/lib/types";
import { HighchartsReact } from "highcharts-react-official";
import Highcharts from "highcharts/esm/highcharts";
import "highcharts/esm/modules/accessibility";
import { mergeWith } from "lodash";

export function Graph({ ratings }: { ratings: Ratings }) {
  const { theme } = useTheme();
  const themeSpecificOptions =
    theme === Theme.LIGHT ? lightThemeOptions : darkThemeOptions;

  return (
    <div className="relative flex max-h-[400px] min-h-[250px] flex-1">
      <HighchartsReact
        highcharts={Highcharts}
        containerProps={{ style: { height: "100%", width: "100%" } }}
        options={{
          series: parseRatings(ratings),
          ...mergeOptions(
            Highcharts.defaultOptions,
            commonOptions,
            themeSpecificOptions,
          ),
        }}
      />
    </div>
  );
}

interface Point {
  x: number;
  y?: number;
  custom?: { episode: Episode };
}

/**
 * Transform data into a format that Highcharts understands.
 */
function parseRatings(ratings: Ratings): Highcharts.SeriesSplineOptions[] {
  let i = 1;
  const allSeries: Highcharts.SeriesSplineOptions[] = [];
  for (const [seasonNumber, seasonRatings] of Object.entries(
    ratings.allEpisodeRatings,
  )) {
    const data = [];
    for (const episode of Object.values(seasonRatings)) {
      if (episode.numVotes == 0) {
        // ignore episodes without ratings
        continue;
      }

      data.push({
        x: i,
        y: episode.rating,
        custom: {
          episode: episode,
        },
      });
      i++;
    }

    const series: Highcharts.SeriesSplineOptions = {
      name: "Season " + seasonNumber,
      type: "spline",
      data: data,
    };
    if (data.length > 0) {
      allSeries.push(series);
    }
  }
  return allSeries;
}

const commonOptions: Highcharts.Options = {
  chart: {
    backgroundColor: "rgba(0,0,0,0)",
    zooming: {
      type: "x",
      mouseWheel: true,
    },
    panning: {
      enabled: true,
      type: "xy",
    },
  },
  title: {
    text: "",
  },

  accessibility: {
    description: "A graph showing all the episode ratings of TV show",
  },

  plotOptions: {
    spline: {
      animation: false,
      dataLabels: {
        enabled: true,
      },
    },
  },

  xAxis: {
    visible: false,
  },

  yAxis: {
    title: {
      text: "",
    },
    max: 10,
    tickInterval: 1,
  },

  tooltip: {
    shared: false,
    headerFormat: "",
    followTouchMove: false, // Allow panning on mobile
    footerFormat: "",
    valueDecimals: 2,
    // CAN NOT BE AN ARROW FUNCTION BECAUSE OF 'THIS' KEYWORD
    pointFormatter: function (this: Point) {
      const episode = this.custom?.episode;
      if (episode) {
        return `
                    ${episode.title} (s${episode.seasonNum.toString()}e${episode.episodeNum.toString()})
                    <br><br>
                    Rating: ${episode.rating.toFixed(1)} (${episode.numVotes.toLocaleString()} votes)
                `;
      } else {
        return "Error: Missing Data";
      }
    },
  },

  credits: {
    enabled: false,
  },
};

const lightThemeOptions: Highcharts.Options = {
  plotOptions: {
    spline: {
      dataLabels: {
        style: {
          color: "rgb(0, 0, 0)",
        },
      },
    },
  },

  legend: {
    itemStyle: {
      color: "rgb(0,0,0)",
    },
  },
};

const darkThemeOptions: Highcharts.Options = {
  colors: [
    "#7CEA9C",
    "#50B2C0",
    "rgb(114, 78, 145)",
    "hsl(45, 93%, 58%)",
    "rgb(230, 78, 108)",
  ],

  yAxis: {
    gridLineColor: "#3f3f46",
    labels: {
      style: {
        color: "#d4d4d4",
      },
    },
  },

  plotOptions: {
    spline: {
      dataLabels: {
        style: {
          color: "#d4d4d4",
        },
      },
    },
  },

  tooltip: {
    style: {
      color: "#d4d4d4",
    },
    borderWidth: 1,
    borderColor: "#d4d4d4",
    backgroundColor: "#171717",
  },

  legend: {
    itemStyle: {
      color: "#d4d4d4",
    },
    itemHoverStyle: {
      color: "#fafafa",
    },
  },
};

/**
 * Modified version of lodash's recursive merge function. In lodash's version,
 * it would merge together two arrays. In this version, if two arrays are
 * encountered, it just replaces the original with the new array. The main use
 * case for this function is when merging the dark theme config object I want it
 * to replace all the colors not just add dark theme colors to the original set
 * of default colors.
 */
function mergeOptions<T>(...options: [T, T, T]): Highcharts.Options {
  // merge mutates the first param so pass in any empty object {} instead.
  return mergeWith(
    {},
    ...options,
    (
      obj: Highcharts.Options,
      src: Highcharts.Options,
    ): Highcharts.Options | undefined => {
      if (Array.isArray(obj) && Array.isArray(src)) {
        return src;
      } else {
        return undefined;
      }
    },
  );
}
