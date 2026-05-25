"use client";

import { useQuery } from "@tanstack/react-query";

import { getLatestSyncExecutions } from "@/services/sync";

export function useLatestSyncExecutions() {
  return useQuery({
    queryKey: ["dashboard", "latest-sync-executions"],
    queryFn: getLatestSyncExecutions,
    refetchInterval: 30_000,
  });
}
