export type CheckInType =
  | "CHECKIN"
  | "CHECKOUT"
  | "VISIT_COMPLETED"
  | "STOP_DETECTED"
  | "GEOFENCE_ENTER"
  | "GEOFENCE_EXIT"
  | "LOW_BATTERY"
  | "SIGNAL_LOST";

export type CheckInSource = "MANUAL" | "GPS_SYNC" | "EVENT_SYNC";

export type CheckIn = {
  id: string;
  agentId: string;
  type: CheckInType;
  source: CheckInSource;
  latitude: number | null;
  longitude: number | null;
  address: string | null;
  accuracy: number | null;
  speed: number | null;
  notes: string | null;
  distanceFromPrevious: number | null;
  externalEventId: string | null;
  occurredAt: string;
  syncedAt: string;
};
