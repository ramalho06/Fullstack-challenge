import { apiFetch } from "@/services/api";
import type { RouteResponse } from "@/types/route";

export function getAgentRoute(agentId: string, date: string) {
  return apiFetch<RouteResponse>(
    `/api/v1/agents/${agentId}/route?date=${encodeURIComponent(date)}`,
  );
}
