export type GeofenceType = "POLYGON" | "CIRCLE";

export type Geofence = {
  id: string;
  externalId: string;
  name: string;
  type: GeofenceType;
  coordinatesJson: string;
  alertOnEnter: boolean;
  alertOnExit: boolean;
  assignedTeams: string | null;
  syncedAt: string;
  createdAt: string;
  updatedAt: string;
};

export type GeofenceFilters = {
  type?: GeofenceType;
  page?: number;
  size?: number;
  sort?: string;
};
