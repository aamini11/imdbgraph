import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  experimental: {
    ppr: "incremental",
  },
  // Don't display dev indicators in tests since it messes with screenshots
  devIndicators:
    process.env.NODE_ENV === "test" ? false : { position: "bottom-left" },
};

export default nextConfig;
