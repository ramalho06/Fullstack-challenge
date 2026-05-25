"use client";

import { Search } from "lucide-react";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { AgentStatus } from "@/types/agent";

type AgentFiltersProps = {
  search: string;
  status: AgentStatus | undefined;
  active: boolean | undefined;
  onSearchChange: (value: string) => void;
  onStatusChange: (value: AgentStatus | undefined) => void;
  onActiveChange: (value: boolean | undefined) => void;
  onReset: () => void;
};

const agentStatuses: AgentStatus[] = [
  "ONLINE",
  "PAUSED",
  "SIGNAL_LOST",
  "OFFLINE",
];

export function AgentFilters({
  search,
  status,
  active,
  onSearchChange,
  onStatusChange,
  onActiveChange,
  onReset,
}: AgentFiltersProps) {
  return (
    <div className="grid gap-3 rounded-md border bg-card p-4 md:grid-cols-[minmax(0,1fr)_180px_160px_auto] md:items-end">
      <label className="grid gap-2 text-sm">
        <span className="font-medium">Buscar</span>
        <div className="relative">
          <Search
            className="pointer-events-none absolute top-1/2 left-2.5 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />
          <Input
            className="pl-8"
            onChange={(event) => onSearchChange(event.target.value)}
            placeholder="Nome, email ou equipe"
            value={search}
          />
        </div>
      </label>

      <label className="grid gap-2 text-sm">
        <span className="font-medium">Status</span>
        <Select
          value={status ?? "ALL"}
          onValueChange={(value) =>
            onStatusChange(
              !value || value === "ALL" ? undefined : (value as AgentStatus),
            )
          }
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todos" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            {agentStatuses.map((agentStatus) => (
              <SelectItem key={agentStatus} value={agentStatus}>
                {agentStatus}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </label>

      <label className="grid gap-2 text-sm">
        <span className="font-medium">Ativo</span>
        <Select
          value={
            active === undefined ? "ALL" : active ? "ACTIVE" : "INACTIVE"
          }
          onValueChange={(value) => {
            if (!value || value === "ALL") {
              onActiveChange(undefined);
              return;
            }

            onActiveChange(value === "ACTIVE");
          }}
        >
          <SelectTrigger className="w-full">
            <SelectValue placeholder="Todos" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos</SelectItem>
            <SelectItem value="ACTIVE">Ativos</SelectItem>
            <SelectItem value="INACTIVE">Inativos</SelectItem>
          </SelectContent>
        </Select>
      </label>

      <Button onClick={onReset} type="button" variant="outline">
        Limpar
      </Button>
    </div>
  );
}
