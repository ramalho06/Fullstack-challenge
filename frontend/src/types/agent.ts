export type AgentRole = "TECHNICIAN" | "MAINTENANCE" | "VENDOR" | "INSTALLER";

export type AgentStatus = "ONLINE" | "PAUSED" | "SIGNAL_LOST" | "OFFLINE";

export type Agent = {
  id: string;
  externalId: string;
  name: string;
  role: AgentRole | null;
  team: string | null;
  phone: string | null;
  email: string | null;
  active: boolean;
  status: AgentStatus;
  battery: number | null;
  lastSeen: string | null;
  currentLatitude: number | null;
  currentLongitude: number | null;
  currentAddress: string | null;
  currentAccuracy: number | null;
  currentSpeed: number | null;
  currentLocationUpdatedAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export type AgentFilters = {
  active?: boolean;
  status?: AgentStatus;
  role?: AgentRole;
  team?: string;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
};
