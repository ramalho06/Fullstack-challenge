"use client";

import { Activity, AlertTriangle, CheckCircle2 } from "lucide-react";

import { OperationalStatusBadge } from "@/components/status/status-badges";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { formatDateTime, formatNumber } from "@/lib/formatters";
import type { SyncStatusResponse } from "@/types/sync";

type SyncStatusCardsProps = {
  data?: SyncStatusResponse;
  loading?: boolean;
  error?: boolean;
};

export function SyncStatusCards({ data, loading, error }: SyncStatusCardsProps) {
  const cards = [
    {
      title: "Status geral",
      value: data ? <OperationalStatusBadge status={data.overallStatus} /> : "—",
      description: "Calculado a partir das últimas execuções.",
      icon: Activity,
    },
    {
      title: "Execuções totais",
      value: formatNumber(data?.totalExecutions),
      description: `Último sucesso: ${formatDateTime(data?.lastSuccessfulSyncAt)}`,
      icon: CheckCircle2,
    },
    {
      title: "Falhas totais",
      value: formatNumber(data?.totalFailures),
      description: `Última falha: ${formatDateTime(data?.lastFailedSyncAt)}`,
      icon: AlertTriangle,
    },
  ];

  return (
    <section className="grid gap-4 md:grid-cols-3">
      {cards.map((card) => {
        const Icon = card.icon;

        return (
          <Card key={card.title}>
            <CardHeader className="pb-0">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <CardDescription>{card.title}</CardDescription>
                  <CardTitle className="mt-2 text-2xl font-semibold">
                    {loading ? <Skeleton className="h-8 w-24" /> : card.value}
                  </CardTitle>
                </div>
                <div className="flex size-10 items-center justify-center rounded-md bg-secondary">
                  <Icon
                    className="size-5 text-muted-foreground"
                    aria-hidden="true"
                  />
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <p className="text-xs leading-5 text-muted-foreground">
                {error
                  ? "Não foi possível carregar este indicador."
                  : card.description}
              </p>
            </CardContent>
          </Card>
        );
      })}
    </section>
  );
}
