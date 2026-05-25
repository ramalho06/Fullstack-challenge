export type SyncStatus = "RUNNING" | "SUCCESS" | "FAILED" | "PARTIAL_SUCCESS";

export type SyncType =
  | "AGENTS"
  | "LOCATIONS"
  | "CHECK_INS"
  | "GEOFENCES"
  | "FULL_SYNC";

export type OperationalSyncStatus = "HEALTHY" | "WARNING" | "DEGRADED";

export type SyncExecution = {
  id: number;
  syncType: SyncType;
  status: SyncStatus;
  startedAt: string;
  finishedAt: string | null;
  itemsProcessed: number;
  itemsCreated: number;
  itemsUpdated: number;
  itemsSkipped: number;
  errorMessage: string | null;
  httpStatus: number | null;
  syncTokenBefore: string | null;
  syncTokenAfter: string | null;
  createdAt: string;
  updatedAt: string;
};

export type SyncTypeStatus = {
  syncType: SyncType;
  schedulerEnabled: boolean;
  fixedDelayMs: number;
  initialDelayMs: number;
  lastExecution: SyncExecution | null;
};

export type SyncStatusResponse = {
  overallStatus: OperationalSyncStatus;
  lastSuccessfulSyncAt: string | null;
  lastFailedSyncAt: string | null;
  totalExecutions: number;
  totalFailures: number;
  syncs: SyncTypeStatus[];
};
