import { apiFetch } from "@/services/api";
import type { PageResponse } from "@/types/api";
import type { Geofence, GeofenceFilters } from "@/types/geofence";

export function getGeofences(filters: GeofenceFilters = {}) {
  const params = new URLSearchParams();

  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });

  const query = params.toString();

  return apiFetch<PageResponse<Geofence>>(
    `/api/v1/geofences${query ? `?${query}` : ""}`,
  );
}
