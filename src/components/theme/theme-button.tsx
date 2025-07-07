"use client";

import { Button } from "@/components/button";
import { Moon, Sun } from "lucide-react";
import { useTheme } from "next-themes";
import * as React from "react";

export function ThemeButton() {
  const { setTheme } = useTheme();

  return (
    <Button
      variant="outline"
      size="icon"
      onClick={() => {
        setTheme((prev) => (prev === "light" ? "dark" : "light"));
      }}
    >
      <Sun className="h-[1.2rem] w-[1.2rem] scale-100 rotate-0 transition-all dark:scale-0 dark:-rotate-90" />
      <Moon className="absolute h-[1.2rem] w-[1.2rem] scale-0 rotate-90 transition-all dark:scale-100 dark:rotate-0" />
      <span className="sr-only">Toggle theme</span>
    </Button>
  );
}
