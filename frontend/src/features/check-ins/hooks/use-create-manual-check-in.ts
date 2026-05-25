"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";

import { createManualCheckIn } from "@/services/check-ins";

export function useCreateManualCheckIn() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: createManualCheckIn,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["check-ins"] });
      void queryClient.invalidateQueries({ queryKey: ["dashboard"] });
      toast.success("Check-in registrado com sucesso.");
    },
    onError: () => {
      toast.error("Erro ao registrar check-in.");
    },
  });
}
