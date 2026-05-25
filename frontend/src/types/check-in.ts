export type CheckInType =
  | "CHECKIN"
  | "CHECKOUT"
  | "VISIT_COMPLETED"
  | "STOP_DETECTED"
  | "STOP_ENDED"
  | "GEOFENCE_ENTER"
  | "GEOFENCE_EXIT"
  | "LOW_BATTERY"
  | "SIGNAL_LOST"
  | "SIGNAL_RESTORED";

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

export type CheckInFilters = {
  agentId?: string;
  type?: CheckInType;
  source?: CheckInSource;
  page?: number;
  size?: number;
  sort?: string;
};

export type ManualCheckInPayload = {
  agentId: string;
  type: CheckInType;
  latitude?: number | null;
  longitude?: number | null;
  address?: string | null;
  accuracy?: number | null;
  speed?: number | null;
  notes?: string | null;
};
