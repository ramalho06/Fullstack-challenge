"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import {
  createAgent,
  deactivateAgent,
  updateAgent,
} from "@/services/agents";
import type { AgentWritePayload } from "@/types/agent";

export function useCreateAgent() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createAgent,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["agents"] });
      void queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Agente criado com sucesso.");
    },
    onError: () => {
      toast.error("Erro ao salvar agente.");
    },
  });
}

export function useUpdateAgent() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      id,
      payload,
    }: {
      id: string;
      payload: AgentWritePayload;
    }) => updateAgent(id, payload),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["agents"] });
      void queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Agente atualizado com sucesso.");
    },
    onError: () => {
      toast.error("Erro ao salvar agente.");
    },
  });
}

export function useDeactivateAgent() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: deactivateAgent,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["agents"] });
      void queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Agente desativado com sucesso.");
    },
    onError: () => {
      toast.error("Erro ao desativar agente.");
    },
  });
}
