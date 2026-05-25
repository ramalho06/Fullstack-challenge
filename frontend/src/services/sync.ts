import { apiFetch } from "@/services/api";
import type { SyncStatusResponse } from "@/types/sync";

export function getSyncStatus() {
  return apiFetch<SyncStatusResponse>("/api/v1/sync/status");
}
