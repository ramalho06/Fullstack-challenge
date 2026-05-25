import type { AgentStatus } from "@/types/agent";

export type LocationSource = "GPS_SYNC" | "MANUAL_CHECKIN" | "EVENT_SYNC";

export type CurrentLocation = {
  agentId: string;
  externalId: string;
  name: string;
  latitude: number | null;
  longitude: number | null;
  currentAddress: string | null;
  accuracy: number | null;
  speed: number | null;
  battery: number | null;
  status: AgentStatus;
  lastSeen: string | null;
};
