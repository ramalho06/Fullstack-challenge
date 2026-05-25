import { apiFetch } from "@/services/api";
import type {
  SyncExecution,
  SyncExecutionFilters,
  SyncStatusResponse,
} from "@/types/sync";
import type { PageResponse } from "@/types/api";

export function getSyncStatus() {
  return apiFetch<SyncStatusResponse>("/api/v1/sync/status");
}

export function getLatestSyncExecutions() {
  return apiFetch<SyncExecution[]>("/api/v1/sync/executions/latest");
}

export function getSyncExecutions(filters: SyncExecutionFilters = {}) {
  const params = new URLSearchParams();

  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });

  const query = params.toString();

  return apiFetch<PageResponse<SyncExecution>>(
    `/api/v1/sync/executions${query ? `?${query}` : ""}`,
  );
}
