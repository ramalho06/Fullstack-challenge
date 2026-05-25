import { apiFetch } from "@/services/api";
import type { Agent, AgentFilters, AgentWritePayload } from "@/types/agent";
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

export function createAgent(payload: AgentWritePayload) {
  return apiFetch<Agent>("/api/v1/agents", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateAgent(id: string, payload: AgentWritePayload) {
  return apiFetch<Agent>(`/api/v1/agents/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function deactivateAgent(id: string) {
  return apiFetch<void>(`/api/v1/agents/${id}`, {
    method: "DELETE",
  });
}
