import "@/app/globals.css";
import Providers from "@/app/providers";
import { ThemeButton } from "@/components/theme/theme-button";
import { cn } from "@/lib/utils";
import { Analytics } from "@vercel/analytics/react";
import { SpeedInsights } from "@vercel/speed-insights/next";
import { Metadata } from "next";
import { Geist } from "next/font/google";
import { ReactNode } from "react";

export const metadata: Metadata = {
  title: "IMDB Graph",
  keywords: [
    "imdbgraph.org",
    "imdbgraph.com",
    "IMDB",
    "Graph",
    "IMDB Graph",
    "IMDB Chart",
    "imdbgraph",
    "Visualize",
    "Episode ratings",
    "chart",
    "season",
    "episode",
  ],
  description: "Website to visualize IMDB TV show ratings as a graph",
  icons: "/favicon.ico",
};

const geistSans = Geist({
  subsets: ["latin"],
  variable: "--font-geist-sans",
});

export default function RootLayout(props: { children: ReactNode }) {
  return (
    /**
     * SuppressHydrationWarning is necessary because it is impossible for the
     * server to know what the default theme is. So it will complain about
     * mismatching class="dark" attribute. This only suppresses warnings for the
     * html element and not children. (Only 1 level deep)
     */
    <html lang="en" suppressHydrationWarning>
      <body
        className={cn(
          "bg-background min-h-dvh min-w-80 font-sans antialiased",
          geistSans.variable,
        )}
      >
        <Providers>
          <div className="flex min-h-dvh flex-col">
            {/* Header with theme button in top right corner */}
            <div className="ml-auto p-3">
              <ThemeButton />
            </div>
            {/* Main content */}
            <div className="flex w-full flex-1 flex-col">{props.children}</div>
            {/* Footer */}
            <footer className="w-full px-6 py-6">
              <div className="flex flex-col items-center justify-between gap-4">
                <p className="text-muted-foreground text-center text-sm leading-loose text-balance">
                  Built by{" "}
                  <a
                    href="https://www.linkedin.com/in/aria-amini/"
                    target="_blank"
                    rel="noreferrer"
                    className="font-medium underline underline-offset-4"
                  >
                    Aria
                  </a>
                  . The source code is available on{" "}
                  <a
                    href="https://github.com/aamini11/imdbgraph"
                    target="_blank"
                    rel="noreferrer"
                    className="font-medium underline underline-offset-4"
                  >
                    GitHub
                  </a>
                  .
                </p>
              </div>
            </footer>
          </div>
        </Providers>
        <Analytics />
        <SpeedInsights />
      </body>
    </html>
  );
}
