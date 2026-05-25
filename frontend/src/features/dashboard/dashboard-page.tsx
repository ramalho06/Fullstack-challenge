"use client";

import {
  AlertTriangle,
  Clock3,
  RadioTower,
  UserCheck,
  UsersRound,
  Wifi,
} from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { formatDateTime, formatNumber } from "@/lib/formatters";

import { CurrentLocationsTable } from "./components/current-locations-table";
import { DashboardMetricCard } from "./components/dashboard-metric-card";
import { LatestSyncExecutionsTable } from "./components/latest-sync-executions-table";
import { OperationalStatusBadge } from "./components/status-badges";
import { useAgentsSummary } from "./hooks/use-agents-summary";
import { useCurrentLocations } from "./hooks/use-current-locations";
import { useLatestSyncExecutions } from "./hooks/use-latest-sync-executions";
import { useSyncStatus } from "./hooks/use-sync-status";

export function DashboardPage() {
  const agentsSummary = useAgentsSummary();
  const currentLocations = useCurrentLocations();
  const syncStatus = useSyncStatus();
  const latestSyncExecutions = useLatestSyncExecutions();

  const lastSuccessfulSyncAt = syncStatus.data?.lastSuccessfulSyncAt;
  const lastFailedSyncAt = syncStatus.data?.lastFailedSyncAt;

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-3">
        <div className="flex flex-wrap items-center gap-2">
          {syncStatus.data ? (
            <OperationalStatusBadge status={syncStatus.data.overallStatus} />
          ) : (
            <Badge variant="secondary">Status operacional</Badge>
          )}
          <span className="text-xs text-muted-foreground">
            Dados atualizados automaticamente
          </span>
        </div>

        <div className="flex flex-col gap-2">
          <h1 className="text-3xl font-semibold tracking-normal">Dashboard</h1>
          <p className="max-w-2xl text-sm leading-6 text-muted-foreground">
            Visão operacional das equipes externas com dados reais do backend.
          </p>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        <DashboardMetricCard
          description="Quantidade total encontrada na listagem paginada."
          error={agentsSummary.isError}
          icon={UsersRound}
          loading={agentsSummary.isLoading}
          title="Total de agentes"
          value={formatNumber(agentsSummary.data?.total)}
        />
        <DashboardMetricCard
          description="Agentes marcados como ativos no cadastro."
          error={agentsSummary.isError}
          icon={UserCheck}
          loading={agentsSummary.isLoading}
          title="Agentes ativos"
          value={formatNumber(agentsSummary.data?.active)}
        />
        <DashboardMetricCard
          description="Agentes com status operacional online."
          error={agentsSummary.isError}
          icon={Wifi}
          loading={agentsSummary.isLoading}
          title="Agentes online"
          value={formatNumber(agentsSummary.data?.online)}
        />
        <DashboardMetricCard
          description="Última sincronização finalizada com sucesso."
          error={syncStatus.isError}
          icon={Clock3}
          loading={syncStatus.isLoading}
          title="Última sincronização"
          value={
            lastSuccessfulSyncAt
              ? formatDateTime(lastSuccessfulSyncAt)
              : "Sem sincronização"
          }
        />
        <DashboardMetricCard
          description={
            lastFailedSyncAt
              ? `Última falha em ${formatDateTime(lastFailedSyncAt)}.`
              : "Nenhuma falha registrada."
          }
          error={syncStatus.isError}
          icon={AlertTriangle}
          loading={syncStatus.isLoading}
          title="Falhas de sincronização"
          value={formatNumber(syncStatus.data?.totalFailures)}
        />
      </section>

      <section className="grid gap-4 xl:grid-cols-[minmax(0,1.15fr)_minmax(0,0.85fr)]">
        <LatestSyncExecutionsTable
          error={latestSyncExecutions.isError}
          executions={latestSyncExecutions.data}
          loading={latestSyncExecutions.isLoading}
        />

        <div className="rounded-md border bg-card p-5">
          <div className="flex items-start gap-3">
            <div className="flex size-10 shrink-0 items-center justify-center rounded-md bg-secondary">
              <RadioTower
                className="size-5 text-muted-foreground"
                aria-hidden="true"
              />
            </div>
            <div className="min-w-0">
              <h2 className="text-base font-semibold">Resumo operacional</h2>
              <p className="mt-1 text-sm leading-6 text-muted-foreground">
                O dashboard usa refetch periódico com TanStack Query para manter
                a visão atualizada sem WebSocket ou SSE nesta etapa.
              </p>
              <dl className="mt-4 grid gap-3 text-sm">
                <div className="flex items-center justify-between gap-3">
                  <dt className="text-muted-foreground">Execuções totais</dt>
                  <dd className="font-medium">
                    {formatNumber(syncStatus.data?.totalExecutions)}
                  </dd>
                </div>
                <div className="flex items-center justify-between gap-3">
                  <dt className="text-muted-foreground">Falhas totais</dt>
                  <dd className="font-medium">
                    {formatNumber(syncStatus.data?.totalFailures)}
                  </dd>
                </div>
              </dl>
            </div>
          </div>
        </div>
      </section>

      <CurrentLocationsTable
        error={currentLocations.isError}
        loading={currentLocations.isLoading}
        locations={currentLocations.data}
      />
    </div>
  );
}
