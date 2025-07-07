"use client";

import { ThemeProvider as NextThemesProvider, useTheme } from "next-themes";
import * as React from "react";

export { useTheme };

export enum Theme {
  LIGHT = "light",
  DARK = "dark",
}

export function ThemeProvider({
  children,
  ...props
}: React.ComponentProps<typeof NextThemesProvider>) {
  return (
    <NextThemesProvider
      attribute="class"
      defaultTheme="dark"
      enableSystem={false}
      disableTransitionOnChange
      {...props}
    >
      {children}
    </NextThemesProvider>
  );
}
