"use client";

import { useQuery } from "@tanstack/react-query";

import { getAgents } from "@/services/agents";

export function useAgentsSummary() {
  return useQuery({
    queryKey: ["dashboard", "agents-summary"],
    queryFn: () => getAgents({ page: 0, size: 100 }),
    refetchInterval: 60_000,
    select: (page) => {
      const agents = page.content;

      return {
        agents,
        total: page.totalElements ?? agents.length,
        active: agents.filter((agent) => agent.active).length,
        online: agents.filter((agent) => agent.status === "ONLINE").length,
      };
    },
  });
}
