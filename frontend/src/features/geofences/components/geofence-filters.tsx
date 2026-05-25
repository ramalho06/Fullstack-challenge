"use client";

import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { GeofenceType } from "@/types/geofence";

type GeofenceFiltersProps = {
  type: GeofenceType | undefined;
  onTypeChange: (value: GeofenceType | undefined) => void;
  onReset: () => void;
};

const geofenceTypes: GeofenceType[] = ["CIRCLE", "POLYGON"];

export function GeofenceFilters({
  type,
  onTypeChange,
  onReset,
}: GeofenceFiltersProps) {
  return (
    <div className="grid gap-3 rounded-md border bg-card p-4 md:grid-cols-[220px_auto] md:items-end md:justify-start">
      <label className="grid gap-2 text-sm">
        <span className="font-medium">Tipo</span>
        <Select
          value={type ?? "ALL"}
          onValueChange={(value) =>
            onTypeChange(
              !value || value === "ALL"
                ? undefined
                : (value as GeofenceType),
            )
          }
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todos" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            {geofenceTypes.map((geofenceType) => (
              <SelectItem key={geofenceType} value={geofenceType}>
                {geofenceType}
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
