"use client";

import { MapPinned } from "lucide-react";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { formatMeters, formatNumber } from "@/lib/formatters";
import type { Agent } from "@/types/agent";
import type { RouteResponse } from "@/types/route";

type RouteSummaryCardProps = {
  route?: RouteResponse;
  selectedAgent?: Agent;
  selectedDate: string;
  loading?: boolean;
  error?: boolean;
  requested?: boolean;
};

export function RouteSummaryCard({
  route,
  selectedAgent,
  selectedDate,
  loading,
  error,
  requested,
}: RouteSummaryCardProps) {
  const pointsCount = route?.points.length ?? 0;

  return (
    <Card>
      <CardHeader>
        <div className="flex items-start gap-3">
          <div className="flex size-10 shrink-0 items-center justify-center rounded-md bg-secondary">
            <MapPinned className="size-5 text-muted-foreground" aria-hidden="true" />
          </div>
          <div>
            <CardTitle>Resumo da rota</CardTitle>
            <CardDescription>
              Distância calculada pelo backend com Haversine.
            </CardDescription>
          </div>
        </div>
      </CardHeader>
      <CardContent className="grid gap-3 text-sm">
        {!requested ? (
          <p className="text-muted-foreground">
            Escolha agente e data para carregar uma rota.
          </p>
        ) : null}

        {loading ? (
          <p className="text-muted-foreground">Carregando rota...</p>
        ) : null}

        {error ? (
          <p className="text-destructive">Não foi possível carregar a rota.</p>
        ) : null}

        {requested && !loading && !error && route ? (
          <>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Agente</span>
              <span className="font-medium">
                {selectedAgent?.name ?? route.agentId}
              </span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Data</span>
              <span className="font-medium">{selectedDate}</span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Distância total</span>
              <span className="font-medium">
                {formatMeters(route.totalDistanceMeters)}
              </span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Pontos</span>
              <span className="font-medium">{formatNumber(pointsCount)}</span>
            </div>

            {pointsCount === 0 ? (
              <p className="rounded-md border bg-muted/30 p-3 text-muted-foreground">
                Nenhum ponto encontrado para a rota neste dia.
              </p>
            ) : null}
          </>
        ) : null}
      </CardContent>
    </Card>
  );
}
