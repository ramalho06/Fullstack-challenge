"use client";

import { useQuery } from "@tanstack/react-query";

import {
  getLatestSyncExecutions,
  getSyncExecutions,
  getSyncStatus,
} from "@/services/sync";
import type { SyncExecutionFilters } from "@/types/sync";

export function useSyncStatus() {
  return useQuery({
    queryKey: ["sync", "status"],
    queryFn: getSyncStatus,
    refetchInterval: 30_000,
  });
}

export function useLatestSyncExecutions() {
  return useQuery({
    queryKey: ["sync", "latest-executions"],
    queryFn: getLatestSyncExecutions,
    refetchInterval: 30_000,
  });
}

export function useSyncExecutions(filters: SyncExecutionFilters) {
  return useQuery({
    queryKey: ["sync", "executions", filters],
    queryFn: () => getSyncExecutions(filters),
    refetchInterval: 30_000,
  });
}
