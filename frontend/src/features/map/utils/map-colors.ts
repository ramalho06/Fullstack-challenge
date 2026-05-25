import type { AgentStatus } from "@/types/agent";

export const DEFAULT_MAP_CENTER: [number, number] = [-23.5505, -46.6333];
export const DEFAULT_MAP_ZOOM = 12;

const statusColors: Record<
  AgentStatus,
  { stroke: string; fill: string; label: string }
> = {
  ONLINE: {
    stroke: "#047857",
    fill: "#10b981",
    label: "Online",
  },
  PAUSED: {
    stroke: "#b45309",
    fill: "#f59e0b",
    label: "Pausado",
  },
  SIGNAL_LOST: {
    stroke: "#c2410c",
    fill: "#f97316",
    label: "Sem sinal",
  },
  OFFLINE: {
    stroke: "#52525b",
    fill: "#a1a1aa",
    label: "Offline",
  },
};

export function getAgentStatusColor(status: AgentStatus) {
  return statusColors[status];
}
