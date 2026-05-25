import type { LocationSource } from "@/types/location";

export type RoutePoint = {
  latitude: number;
  longitude: number;
  address: string | null;
  accuracy: number | null;
  speed: number | null;
  timestamp: string;
  source: LocationSource;
  distanceFromPreviousMeters: number;
};

export type RouteResponse = {
  agentId: string;
  date: string;
  totalDistanceMeters: number;
  points: RoutePoint[];
};
