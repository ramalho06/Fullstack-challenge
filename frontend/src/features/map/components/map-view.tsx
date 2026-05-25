"use client";

import {
  CircleMarker,
  MapContainer,
  Polyline,
  Popup,
  TileLayer,
} from "react-leaflet";

import {
  formatDateTime,
  formatOptional,
  formatPercent,
  formatSpeed,
} from "@/lib/formatters";
import type { Geofence } from "@/types/geofence";
import type { CurrentLocation } from "@/types/location";
import type { RoutePoint } from "@/types/route";

import { GeofenceLayer } from "./geofence-layer";
import {
  DEFAULT_MAP_CENTER,
  DEFAULT_MAP_ZOOM,
  getAgentStatusColor,
} from "../utils/map-colors";

type MapViewProps = {
  geofences: Geofence[];
  locations: CurrentLocation[];
  routePoints: RoutePoint[];
  showGeofences: boolean;
};

export default function MapView({
  geofences,
  locations,
  routePoints,
  showGeofences,
}: MapViewProps) {
  const validLocations = locations.filter(
    (location) => location.latitude !== null && location.longitude !== null,
  );
  const routePositions = routePoints
    .filter((point) => point.latitude !== null && point.longitude !== null)
    .map((point) => [point.latitude, point.longitude] as [number, number]);

  return (
    <MapContainer
      center={DEFAULT_MAP_CENTER}
      className="h-[560px] min-h-[420px] w-full rounded-md"
      scrollWheelZoom
      zoom={DEFAULT_MAP_ZOOM}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <GeofenceLayer geofences={geofences} visible={showGeofences} />

      {validLocations.map((location) => {
        const color = getAgentStatusColor(location.status);

        return (
          <CircleMarker
            center={[location.latitude as number, location.longitude as number]}
            key={location.agentId}
            pathOptions={{
              color: color.stroke,
              fillColor: color.fill,
              fillOpacity: 0.82,
              opacity: 0.95,
              weight: 2,
            }}
            radius={10}
          >
            <Popup>
              <div className="grid min-w-56 gap-1 text-sm">
                <strong>{location.name}</strong>
                <span>Status: {color.label}</span>
                <span>Bateria: {formatPercent(location.battery)}</span>
                <span>Velocidade: {formatSpeed(location.speed)}</span>
                <span>
                  Endereço: {formatOptional(location.currentAddress)}
                </span>
                <span>
                  Última atualização: {formatDateTime(location.lastSeen)}
                </span>
              </div>
            </Popup>
          </CircleMarker>
        );
      })}

      {routePositions.length > 1 ? (
        <Polyline
          pathOptions={{ color: "#2563eb", opacity: 0.85, weight: 4 }}
          positions={routePositions}
        />
      ) : null}

      <div className="leaflet-bottom leaflet-left">
        <div className="leaflet-control rounded-md border bg-background/95 px-3 py-2 text-xs text-foreground shadow-sm">
          <div className="grid gap-1">
            <span className="flex items-center gap-2">
              <span className="size-2 rounded-full bg-emerald-500" />
              Agentes
            </span>
            <span className="flex items-center gap-2">
              <span className="h-0.5 w-4 rounded bg-blue-600" />
              Rota do dia
            </span>
            <span className="flex items-center gap-2">
              <span className="h-2 w-4 rounded-sm border border-violet-600 bg-violet-500/20" />
              Geofences
            </span>
          </div>
        </div>
      </div>
    </MapContainer>
  );
}
