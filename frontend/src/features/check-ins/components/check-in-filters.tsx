"use client";

import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { Agent } from "@/types/agent";
import type { CheckInSource, CheckInType } from "@/types/check-in";

type CheckInFiltersProps = {
  agents: Agent[];
  agentId: string | undefined;
  type: CheckInType | undefined;
  source: CheckInSource | undefined;
  onAgentIdChange: (value: string | undefined) => void;
  onTypeChange: (value: CheckInType | undefined) => void;
  onSourceChange: (value: CheckInSource | undefined) => void;
  onReset: () => void;
};

export const checkInTypes: CheckInType[] = [
  "CHECKIN",
  "CHECKOUT",
  "VISIT_COMPLETED",
  "STOP_DETECTED",
  "STOP_ENDED",
  "SIGNAL_LOST",
  "SIGNAL_RESTORED",
  "LOW_BATTERY",
  "GEOFENCE_ENTER",
  "GEOFENCE_EXIT",
];

export const checkInSources: CheckInSource[] = [
  "MANUAL",
  "GPS_SYNC",
  "EVENT_SYNC",
];

export function CheckInFilters({
  agents,
  agentId,
  type,
  source,
  onAgentIdChange,
  onTypeChange,
  onSourceChange,
  onReset,
}: CheckInFiltersProps) {
  return (
    <div className="grid gap-3 rounded-md border bg-card p-4 md:grid-cols-[minmax(0,1fr)_180px_180px_auto] md:items-end">
      <label className="grid gap-2 text-sm">
        <span className="font-medium">Agente</span>
        <Select
          value={agentId ?? "ALL"}
          onValueChange={(value) =>
            onAgentIdChange(!value || value === "ALL" ? undefined : value)
          }
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todos" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            {agents.map((agent) => (
              <SelectItem key={agent.id} value={agent.id}>
                {agent.name} ({agent.status})
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </label>

      <label className="grid gap-2 text-sm">
        <span className="font-medium">Tipo</span>
        <Select
          value={type ?? "ALL"}
          onValueChange={(value) =>
            onTypeChange(
              !value || value === "ALL" ? undefined : (value as CheckInType),
            )
          }
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todos" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            {checkInTypes.map((checkInType) => (
              <SelectItem key={checkInType} value={checkInType}>
                {checkInType}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </label>

      <label className="grid gap-2 text-sm">
        <span className="font-medium">Origem</span>
        <Select
          value={source ?? "ALL"}
          onValueChange={(value) =>
            onSourceChange(
              !value || value === "ALL"
                ? undefined
                : (value as CheckInSource),
            )
          }
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todas" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todas</SelectItem>
            {checkInSources.map((checkInSource) => (
              <SelectItem key={checkInSource} value={checkInSource}>
                {checkInSource}
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
