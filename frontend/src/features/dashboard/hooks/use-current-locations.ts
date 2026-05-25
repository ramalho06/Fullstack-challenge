"use client";

import { useQuery } from "@tanstack/react-query";

import { getCurrentLocations } from "@/services/locations";

export function useCurrentLocations() {
  return useQuery({
    queryKey: ["dashboard", "current-locations"],
    queryFn: getCurrentLocations,
    refetchInterval: 30_000,
  });
}
