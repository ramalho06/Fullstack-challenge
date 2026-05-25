"use client";

import { useQuery } from "@tanstack/react-query";

import { getCheckIns } from "@/services/check-ins";
import type { CheckInFilters } from "@/types/check-in";

export function useCheckIns(filters: CheckInFilters) {
  return useQuery({
    queryKey: ["check-ins", filters],
    queryFn: () => getCheckIns(filters),
  });
}
