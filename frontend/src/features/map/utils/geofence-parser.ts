import type { Geofence } from "@/types/geofence";

type CoordinatePair = [number, number];

export type ParsedCircleGeofence = {
  kind: "CIRCLE";
  center: CoordinatePair;
  radius: number;
  geofence: Geofence;
};

export type ParsedPolygonGeofence = {
  kind: "POLYGON";
  positions: CoordinatePair[];
  geofence: Geofence;
};

export type ParsedGeofence = ParsedCircleGeofence | ParsedPolygonGeofence;

function warnInvalidGeofence(geofence: Geofence, reason: string) {
  if (process.env.NODE_ENV !== "production") {
    console.warn(`Invalid geofence ${geofence.id}: ${reason}`);
  }
}

function isNumber(value: unknown): value is number {
  return typeof value === "number" && Number.isFinite(value);
}

function toLeafletPosition(value: unknown): CoordinatePair | null {
  if (!Array.isArray(value) || value.length !== 2) {
    return null;
  }

  const [longitude, latitude] = value;

  if (!isNumber(longitude) || !isNumber(latitude)) {
    return null;
  }

  return [latitude, longitude];
}

function getPolygonCoordinates(parsed: unknown): unknown {
  if (Array.isArray(parsed)) {
    return parsed;
  }

  if (typeof parsed !== "object" || parsed === null) {
    return null;
  }

  const coordinates = (parsed as { coordinates?: unknown }).coordinates;

  if (
    Array.isArray(coordinates) &&
    coordinates.length === 1 &&
    Array.isArray(coordinates[0]) &&
    Array.isArray(coordinates[0][0])
  ) {
    return coordinates[0];
  }

  return coordinates;
}

export function parseGeofenceCoordinates(
  geofence: Geofence,
): ParsedGeofence | null {
  let parsed: unknown;

  try {
    parsed = JSON.parse(geofence.coordinatesJson);
  } catch {
    warnInvalidGeofence(geofence, "coordinatesJson is not valid JSON");
    return null;
  }

  if (geofence.type === "CIRCLE") {
    if (typeof parsed !== "object" || parsed === null) {
      warnInvalidGeofence(geofence, "circle coordinates must be an object");
      return null;
    }

    const circle = parsed as { center?: unknown; radius?: unknown };
    const center = toLeafletPosition(circle.center);

    if (!center) {
      warnInvalidGeofence(geofence, "circle center must be [longitude, latitude]");
      return null;
    }

    if (!isNumber(circle.radius) || circle.radius <= 0) {
      warnInvalidGeofence(geofence, "circle radius must be a positive number");
      return null;
    }

    return {
      kind: "CIRCLE",
      center,
      radius: circle.radius,
      geofence,
    };
  }

  if (geofence.type === "POLYGON") {
    const coordinates = getPolygonCoordinates(parsed);

    if (!Array.isArray(coordinates)) {
      warnInvalidGeofence(geofence, "polygon coordinates must be an array");
      return null;
    }

    const positions = coordinates
      .map((coordinate) => toLeafletPosition(coordinate))
      .filter((coordinate): coordinate is CoordinatePair => coordinate !== null);

    if (positions.length < 3) {
      warnInvalidGeofence(geofence, "polygon must contain at least 3 valid points");
      return null;
    }

    return {
      kind: "POLYGON",
      positions,
      geofence,
    };
  }

  warnInvalidGeofence(geofence, "unsupported geofence type");
  return null;
}

export function countInvalidGeofences(geofences: Geofence[]) {
  return geofences.filter((geofence) => !parseGeofenceCoordinates(geofence))
    .length;
}
