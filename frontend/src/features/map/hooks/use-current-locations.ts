"use client";

import { useQuery } from "@tanstack/react-query";

import { getCurrentLocations } from "@/services/locations";

export function useCurrentLocations() {
  return useQuery({
    queryKey: ["current-locations"],
    queryFn: getCurrentLocations,
    refetchInterval: 30_000,
  });
}
