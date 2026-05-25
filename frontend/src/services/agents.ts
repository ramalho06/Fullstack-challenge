import { apiFetch } from "@/services/api";
import type { Agent, AgentFilters } from "@/types/agent";
import type { PageResponse } from "@/types/api";

export function getAgents(filters: AgentFilters = {}) {
  const params = new URLSearchParams();

  Object.entries(filters).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });

  const query = params.toString();

  return apiFetch<PageResponse<Agent>>(
    `/api/v1/agents${query ? `?${query}` : ""}`,
  );
}

export const listAgents = getAgents;
