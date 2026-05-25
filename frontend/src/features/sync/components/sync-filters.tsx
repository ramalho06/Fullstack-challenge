"use client";

import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { SyncStatus, SyncType } from "@/types/sync";

type SyncFiltersProps = {
  syncType: SyncType | undefined;
  status: SyncStatus | undefined;
  onSyncTypeChange: (value: SyncType | undefined) => void;
  onStatusChange: (value: SyncStatus | undefined) => void;
  onReset: () => void;
};

const syncTypes: SyncType[] = [
  "AGENTS",
  "LOCATIONS",
  "CHECK_INS",
  "GEOFENCES",
  "FULL_SYNC",
];

const syncStatuses: SyncStatus[] = [
  "RUNNING",
  "SUCCESS",
  "FAILED",
  "PARTIAL_SUCCESS",
];

export function SyncFilters({
  syncType,
  status,
  onSyncTypeChange,
  onStatusChange,
  onReset,
}: SyncFiltersProps) {
  return (
    <div className="grid gap-3 rounded-md border bg-card p-4 md:grid-cols-[220px_220px_auto] md:items-end md:justify-start">
      <label className="grid gap-2 text-sm">
        <span className="font-medium">Tipo</span>
        <Select
          value={syncType ?? "ALL"}
          onValueChange={(value) =>
            onSyncTypeChange(
              !value || value === "ALL" ? undefined : (value as SyncType),
            )
          }
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todos" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            {syncTypes.map((type) => (
              <SelectItem key={type} value={type}>
                {type}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </label>

      <label className="grid gap-2 text-sm">
        <span className="font-medium">Status</span>
        <Select
          value={status ?? "ALL"}
          onValueChange={(value) =>
            onStatusChange(
              !value || value === "ALL" ? undefined : (value as SyncStatus),
            )
          }
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todos" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            {syncStatuses.map((syncStatus) => (
              <SelectItem key={syncStatus} value={syncStatus}>
                {syncStatus}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </label>

      <Button onClick={onReset} type="button" variant="outline">
        Limpar
      </Button>
    </div>
  );
}
