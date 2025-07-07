"use client";

import { LoadingSpinner } from "@/components/icons";
import { fetchSuggestions } from "@/lib/data/suggestions";
import { formatYears } from "@/lib/data/types";
import { cn } from "@/lib/utils";
import { useQuery, keepPreviousData } from "@tanstack/react-query";
import { useCombobox } from "downshift";
import { Search as SearchIcon } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState, useEffect, useDeferredValue } from "react";

/**
 * https://www.w3.org/WAI/ARIA/apg/patterns/combobox/examples/combobox-autocomplete-list/
 */
export function SearchBar({ autoFocus = false }: { autoFocus?: boolean }) {
  const router = useRouter();
  const [inputValue, setInputValue] = useState("");
  const deferredValue = useDeferredValue(inputValue);

  const {
    isFetching,
    data: searchResults,
    error,
  } = useQuery({
    queryKey: ["suggestions", deferredValue],
    queryFn: () => fetchSuggestions(deferredValue),
    placeholderData: keepPreviousData,
    enabled: !!inputValue,
  });

  const {
    isOpen,
    getLabelProps,
    getMenuProps,
    getInputProps,
    highlightedIndex,
    getItemProps,
  } = useCombobox({
    items: searchResults ?? [],
    inputValue,
    getA11yStatusMessage: () => {
      const resultCount = searchResults?.length ?? 0;
      if (error) {
        return "Something went wrong. Please try again.";
      }
      if (!resultCount) {
        return "No TV shows found.";
      }
      return `${resultCount.toString()} TV show${
        resultCount === 1 ? " is" : "s are"
      } available.`;
    },
    onInputValueChange: ({ inputValue }) => {
      setInputValue(inputValue);
    },
    onSelectedItemChange: ({ selectedItem }) => {
      if (selectedItem) {
        // Navigate to ratings page when an item is selected
        router.push(`/ratings?id=${selectedItem.imdbId}`);
      }
    },
    itemToString: (item) => item?.title ?? "",
  });

  // Optimize page navigation by prefetching the ratings page
  useEffect(() => {
    router.prefetch("/ratings");
  }, [router]);

  return (
    <div className="bg-background text-popover-foreground relative flex h-full w-full flex-col text-sm">
      {/* Hidden label for accessibility */}
      <label {...getLabelProps()} className="sr-only">
        Search for TV shows
      </label>

      {/* Search Bar */}
      <search
        className={cn(
          "placeholder:text-muted-foreground selection:bg-primary selection:text-primary-foreground dark:bg-input/30 border-input flex h-10 w-full min-w-0 items-center rounded-xl border bg-transparent px-3 py-1 text-base shadow-xs transition-[color,box-shadow] outline-none md:text-sm",
          "has-focus-visible:border-ring has-focus-visible:ring-ring/50 has-focus-visible:ring-[3px]",
          "has-disabled:pointer-events-none has-disabled:cursor-not-allowed has-disabled:opacity-50",
          "has-aria-invalid:ring-destructive/20 dark:has-aria-invalid:ring-destructive/40 has-aria-invalid:border-destructive",
          "file:text-foreground file:inline-flex file:h-7 file:border-0 file:bg-transparent file:text-sm file:font-medium",
        )}
      >
        <SearchIcon className="mr-2 h-4 w-4 shrink-0 opacity-50" />
        <input
          aria-invalid={!!error}
          autoFocus={autoFocus}
          className="flex-1 outline-none"
          placeholder="Search for any TV show..."
          {...getInputProps()}
        />
        <LoadingSpinner
          className={cn("px-[2px]", { invisible: !isFetching })}
        />
      </search>

      {error && (
        <div
          aria-live="polite"
          className="text-destructive px-2 py-1.5 text-center"
        >
          Something went wrong. Please try again.
        </div>
      )}

      {/* Dropdown Menu */}
      {!error && (
        <ul
          className={cn(
            "bg-popover absolute top-full right-0 left-0 z-50 mt-3 w-full rounded-xl border p-2 shadow-lg",
            {
              hidden:
                !isOpen ||
                !deferredValue ||
                (isFetching && !searchResults?.length),
            },
          )}
          {...getMenuProps()}
        >
          {inputValue && searchResults?.length === 0 && (
            <div className="text-foreground/60 px-2 py-1.5 text-center">
              No TV Shows Found.
            </div>
          )}
          {searchResults?.map((show, index) => (
            <li
              key={show.imdbId}
              className={cn(
                "text-foreground/60 flex w-full cursor-pointer items-center justify-between gap-2 rounded-md px-2 py-1.5 text-sm outline-none select-none",
                "disabled:pointer-events-none disabled:opacity-50",
                "[&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0",
                {
                  "opacity-50": isFetching,
                  "bg-accent text-accent-foreground":
                    highlightedIndex === index,
                  "hover:bg-foreground/5": highlightedIndex !== index,
                },
              )}
              {...getItemProps({ item: show, index })}
            >
              {/* Show Title + Years */}
              <div className="flex flex-1 flex-col">
                <span className="break-words">{show.title}&nbsp;</span>
                <span className="text-foreground/40 text-xs">
                  {formatYears(show)}
                </span>
              </div>
              {/* 1-10 Rating + Blue Star Icon */}
              <div className="flex shrink-0 items-center space-x-1 text-sm">
                <dt>
                  <span className="sr-only">Star rating</span>
                  <svg
                    className="text-sky-500"
                    width="16"
                    height="20"
                    fill="currentColor"
                  >
                    <path d="M7.05 3.691c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.372 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.539 1.118l-2.8-2.034a1 1 0 00-1.176 0l-2.8 2.034c-.783.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.363-1.118L.98 9.483c-.784-.57-.381-1.81.587-1.81H5.03a1 1 0 00.95-.69L7.05 3.69z" />
                  </svg>
                </dt>
                <dd>{`${show.rating.toFixed(1)} / 10.0`}</dd>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
