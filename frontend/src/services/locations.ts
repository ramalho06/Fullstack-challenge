import { apiFetch } from "@/services/api";
import type { CurrentLocation } from "@/types/location";

export function getCurrentLocations() {
  return apiFetch<CurrentLocation[]>("/api/v1/locations");
}
