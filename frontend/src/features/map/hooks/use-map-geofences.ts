"use client";

import { useQuery } from "@tanstack/react-query";

import { getGeofences } from "@/services/geofences";

export function useMapGeofences() {
  return useQuery({
    queryKey: ["map-geofences"],
    queryFn: () => getGeofences({ page: 0, size: 100 }),
    staleTime: 5 * 60 * 1000,
  });
}
