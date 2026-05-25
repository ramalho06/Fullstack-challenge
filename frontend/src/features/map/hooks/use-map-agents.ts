"use client";

import { useAgents } from "@/features/agents/hooks/use-agents";

export function useMapAgents() {
  return useAgents({ page: 0, size: 100 });
}
