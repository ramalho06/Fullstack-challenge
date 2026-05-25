"use client";

import { useQuery } from "@tanstack/react-query";

import { getAgentRoute } from "@/services/routes";

export function useAgentRoute(
  agentId: string | null,
  date: string | null,
  enabled: boolean,
) {
  return useQuery({
    queryKey: ["agent-route", agentId, date],
    queryFn: () => getAgentRoute(agentId as string, date as string),
    enabled: enabled && Boolean(agentId && date),
  });
}
