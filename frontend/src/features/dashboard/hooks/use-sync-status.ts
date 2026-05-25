"use client";

import { useQuery } from "@tanstack/react-query";

import { getSyncStatus } from "@/services/sync";

export function useSyncStatus() {
  return useQuery({
    queryKey: ["dashboard", "sync-status"],
    queryFn: getSyncStatus,
    refetchInterval: 30_000,
  });
}
