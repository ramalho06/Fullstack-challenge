"use client";

import { useQuery } from "@tanstack/react-query";

import { getAgents } from "@/services/agents";
import type { AgentFilters } from "@/types/agent";

export function useAgents(filters: AgentFilters) {
  return useQuery({
    queryKey: ["agents", filters],
    queryFn: () => getAgents(filters),
  });
}
