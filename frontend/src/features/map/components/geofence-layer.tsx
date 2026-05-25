"use client";

import { useMemo } from "react";
import { Circle, Polygon, Popup } from "react-leaflet";

import { formatBoolean, formatOptional } from "@/lib/formatters";
import type { Geofence } from "@/types/geofence";

import {
  parseGeofenceCoordinates,
  type ParsedGeofence,
} from "../utils/geofence-parser";

type GeofenceLayerProps = {
  geofences: Geofence[];
  visible: boolean;
};

export function GeofenceLayer({ geofences, visible }: GeofenceLayerProps) {
  const parsedGeofences = useMemo(
    () =>
      geofences
        .map((geofence) => parseGeofenceCoordinates(geofence))
        .filter(
          (geofence): geofence is ParsedGeofence => geofence !== null,
        ),
    [geofences],
  );

  if (!visible) {
    return null;
  }

  return (
    <>
      {parsedGeofences.map((parsed) => {
        const popup = (
          <Popup>
            <div className="grid min-w-52 gap-1 text-sm">
              <strong>{parsed.geofence.name}</strong>
              <span>Tipo: {parsed.geofence.type}</span>
              <span>
                Alertar entrada: {formatBoolean(parsed.geofence.alertOnEnter)}
              </span>
              <span>
                Alertar saída: {formatBoolean(parsed.geofence.alertOnExit)}
              </span>
              <span>
                Equipes atribuídas:{" "}
                {formatOptional(parsed.geofence.assignedTeams)}
              </span>
            </div>
          </Popup>
        );

        if (parsed.kind === "CIRCLE") {
          return (
            <Circle
              center={parsed.center}
              key={parsed.geofence.id}
              pathOptions={{
                color: "#2563eb",
                fillColor: "#3b82f6",
                fillOpacity: 0.12,
                weight: 2,
              }}
              radius={parsed.radius}
            >
              {popup}
            </Circle>
          );
        }

        return (
          <Polygon
            key={parsed.geofence.id}
            pathOptions={{
              color: "#7c3aed",
              fillColor: "#8b5cf6",
              fillOpacity: 0.12,
              weight: 2,
            }}
            positions={parsed.positions}
          >
            {popup}
          </Polygon>
        );
      })}
    </>
  );
}
