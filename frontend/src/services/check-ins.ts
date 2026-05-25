import { apiFetch } from "@/services/api";
import type {
  CheckIn,
  CheckInFilters,
  ManualCheckInPayload,
} from "@/types/check-in";
import type { PageResponse } from "@/types/api";

export function getCheckIns(filters: CheckInFilters = {}) {
  const params = new URLSearchParams();

  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });

  const query = params.toString();

  return apiFetch<PageResponse<CheckIn>>(
    `/api/v1/check-ins${query ? `?${query}` : ""}`,
  );
}

export function createManualCheckIn(payload: ManualCheckInPayload) {
  return apiFetch<CheckIn>("/api/v1/check-ins", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
