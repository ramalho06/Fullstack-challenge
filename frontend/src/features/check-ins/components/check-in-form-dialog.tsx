"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import type { Agent } from "@/types/agent";
import type { CheckInType, ManualCheckInPayload } from "@/types/check-in";

import { checkInTypes } from "./check-in-filters";

const numericText = z.string().trim();

const checkInFormSchema = z.object({
  agentId: z.string().min(1, "Selecione um agente."),
  type: z.enum(checkInTypes),
  latitude: numericText.refine(
    (value) => value === "" || isNumberInRange(value, -90, 90),
    "Latitude deve estar entre -90 e 90.",
  ),
  longitude: numericText.refine(
    (value) => value === "" || isNumberInRange(value, -180, 180),
    "Longitude deve estar entre -180 e 180.",
  ),
  address: z.string().trim().optional(),
  accuracy: numericText.refine(
    (value) => value === "" || isNumberAtLeast(value, 0),
    "Precisão deve ser maior ou igual a 0.",
  ),
  speed: numericText.refine(
    (value) => value === "" || isNumberAtLeast(value, 0),
    "Velocidade deve ser maior ou igual a 0.",
  ),
  notes: z.string().trim().optional(),
});

type CheckInFormValues = z.infer<typeof checkInFormSchema>;

type CheckInFormDialogProps = {
  agents: Agent[];
  open: boolean;
  submitting?: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (payload: ManualCheckInPayload) => void;
};

const defaultValues: CheckInFormValues = {
  agentId: "",
  type: "CHECKIN",
  latitude: "",
  longitude: "",
  address: "",
  accuracy: "",
  speed: "",
  notes: "",
};

export function CheckInFormDialog({
  agents,
  open,
  submitting,
  onOpenChange,
  onSubmit,
}: CheckInFormDialogProps) {
  const form = useForm<CheckInFormValues>({
    resolver: zodResolver(checkInFormSchema),
    defaultValues,
  });

  useEffect(() => {
    if (open) {
      form.reset(defaultValues);
    }
  }, [form, open]);

  function handleSubmit(values: CheckInFormValues) {
    onSubmit({
      agentId: values.agentId,
      type: values.type,
      latitude: parseOptionalNumber(values.latitude),
      longitude: parseOptionalNumber(values.longitude),
      address: normalizeText(values.address),
      accuracy: parseOptionalNumber(values.accuracy),
      speed: parseOptionalNumber(values.speed),
      notes: normalizeText(values.notes),
    });
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Novo check-in manual</DialogTitle>
          <DialogDescription>
            Registre um evento manual. Coordenadas são opcionais; quando válidas,
            o backend pode gerar histórico de localização.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form
            className="grid gap-4"
            onSubmit={form.handleSubmit(handleSubmit)}
          >
            <div className="grid gap-4 md:grid-cols-2">
              <FormField
                control={form.control}
                name="agentId"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Agente</FormLabel>
                    <Select
                      value={field.value || null}
                      onValueChange={(value) => field.onChange(value ?? "")}
                    >
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue placeholder="Selecione o agente" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {agents.map((agent) => (
                          <SelectItem key={agent.id} value={agent.id}>
                            {agent.name} ({agent.status})
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="type"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Tipo</FormLabel>
                    <Select
                      value={field.value}
                      onValueChange={(value) =>
                        field.onChange(value as CheckInType)
                      }
                    >
                      <FormControl>
                        <SelectTrigger className="w-full">
                          <SelectValue placeholder="Selecione" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {checkInTypes.map((type) => (
                          <SelectItem key={type} value={type}>
                            {type}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <NumberField control={form.control} label="Latitude" name="latitude" />
              <NumberField
                control={form.control}
                label="Longitude"
                name="longitude"
              />
              <NumberField control={form.control} label="Precisão" name="accuracy" />
              <NumberField control={form.control} label="Velocidade" name="speed" />

              <FormField
                control={form.control}
                name="address"
                render={({ field }) => (
                  <FormItem className="md:col-span-2">
                    <FormLabel>Endereço</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="Av. Paulista, 1000 - São Paulo, SP"
                        {...field}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="notes"
                render={({ field }) => (
                  <FormItem className="md:col-span-2">
                    <FormLabel>Notas</FormLabel>
                    <FormControl>
                      <Textarea placeholder="Observações do check-in" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <DialogFooter>
              <Button
                disabled={submitting}
                onClick={() => onOpenChange(false)}
                type="button"
                variant="outline"
              >
                Cancelar
              </Button>
              <Button disabled={submitting} type="submit">
                {submitting ? "Registrando..." : "Registrar"}
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
}

type NumberFieldProps = {
  control: ReturnType<typeof useForm<CheckInFormValues>>["control"];
  name: "latitude" | "longitude" | "accuracy" | "speed";
  label: string;
};

function NumberField({ control, name, label }: NumberFieldProps) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Input inputMode="decimal" placeholder="Opcional" {...field} />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

function normalizeText(value?: string) {
  const trimmed = value?.trim();
  return trimmed ? trimmed : null;
}

function parseOptionalNumber(value: string) {
  const trimmed = value.trim();
  return trimmed ? Number(trimmed) : null;
}

function isNumberInRange(value: string, min: number, max: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= min && parsed <= max;
}

function isNumberAtLeast(value: string, min: number) {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed >= min;
}
