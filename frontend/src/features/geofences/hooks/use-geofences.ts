"use client";

import { useQuery } from "@tanstack/react-query";

import { getGeofences } from "@/services/geofences";
import type { GeofenceFilters } from "@/types/geofence";

export function useGeofences(filters: GeofenceFilters) {
  return useQuery({
    queryKey: ["geofences", filters],
    queryFn: () => getGeofences(filters),
  });
}
