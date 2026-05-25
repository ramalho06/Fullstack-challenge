"use client";

import { Route } from "lucide-react";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import type { Agent } from "@/types/agent";

type AgentRouteControlsProps = {
  agents: Agent[];
  selectedAgentId: string;
  selectedDate: string;
  showGeofences: boolean;
  geofenceCount: number;
  geofencesError?: boolean;
  geofencesLoading?: boolean;
  loadingAgents?: boolean;
  loadingRoute?: boolean;
  onAgentChange: (agentId: string) => void;
  onDateChange: (date: string) => void;
  onLoadRoute: () => void;
  onShowGeofencesChange: (visible: boolean) => void;
};

export function AgentRouteControls({
  agents,
  selectedAgentId,
  selectedDate,
  showGeofences,
  geofenceCount,
  geofencesError,
  geofencesLoading,
  loadingAgents,
  loadingRoute,
  onAgentChange,
  onDateChange,
  onLoadRoute,
  onShowGeofencesChange,
}: AgentRouteControlsProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Rota do dia</CardTitle>
        <CardDescription>
          Selecione um agente e uma data para desenhar o trajeto consolidado.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-3 md:grid-cols-[minmax(0,1fr)_180px_auto_auto] md:items-end">
        <label className="grid gap-2 text-sm">
          <span className="font-medium">Agente</span>
          <Select
            disabled={loadingAgents}
            value={selectedAgentId || null}
            onValueChange={(value) => onAgentChange(value ?? "")}
          >
            <SelectTrigger className="w-full">
              <SelectValue
                placeholder={
                  loadingAgents ? "Carregando agentes..." : "Selecione"
                }
              />
            </SelectTrigger>
            <SelectContent>
              {agents.map((agent) => (
                <SelectItem key={agent.id} value={agent.id}>
                  {agent.name} ({agent.status})
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </label>

        <label className="grid gap-2 text-sm">
          <span className="font-medium">Data</span>
          <Input
            onChange={(event) => onDateChange(event.target.value)}
            type="date"
            value={selectedDate}
          />
        </label>

        <Button
          disabled={!selectedAgentId || !selectedDate || loadingRoute}
          onClick={onLoadRoute}
          type="button"
        >
          <Route aria-hidden="true" />
          {loadingRoute ? "Carregando..." : "Carregar rota"}
        </Button>

        <label className="flex min-h-10 items-center gap-2 rounded-md border px-3 text-sm">
          <input
            checked={showGeofences}
            className="size-4 accent-primary"
            onChange={(event) => onShowGeofencesChange(event.target.checked)}
            type="checkbox"
          />
          <span className="grid leading-tight">
            <span className="font-medium">Mostrar geofences</span>
            <span className="text-xs text-muted-foreground">
              {geofencesLoading
                ? "Carregando..."
                : geofencesError
                  ? "Falha ao carregar"
                  : `${geofenceCount} disponível(is)`}
            </span>
          </span>
        </label>
      </CardContent>
    </Card>
  );
}
