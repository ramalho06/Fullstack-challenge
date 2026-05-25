import { apiFetch } from "@/services/api";
import type { SyncExecution, SyncStatusResponse } from "@/types/sync";

export function getSyncStatus() {
  return apiFetch<SyncStatusResponse>("/api/v1/sync/status");
}

export function getLatestSyncExecutions() {
  return apiFetch<SyncExecution[]>("/api/v1/sync/executions/latest");
}
