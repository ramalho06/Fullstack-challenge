"use client";

import { RadioTower } from "lucide-react";
import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import type { SyncStatus, SyncType } from "@/types/sync";

import { SyncExecutionsTable, pageFromExecutions } from "./components/sync-executions-table";
import { SyncFilters } from "./components/sync-filters";
import { SyncStatusCards } from "./components/sync-status-cards";
import {
  useLatestSyncExecutions,
  useSyncExecutions,
  useSyncStatus,
} from "./hooks/use-sync-monitoring";

const pageSize = 10;

export function SyncPage() {
  const [page, setPage] = useState(0);
  const [syncType, setSyncType] = useState<SyncType | undefined>();
  const [status, setStatus] = useState<SyncStatus | undefined>();

  const syncStatusQuery = useSyncStatus();
  const latestExecutionsQuery = useLatestSyncExecutions();
  const executionsQuery = useSyncExecutions({
    page,
    size: pageSize,
    sort: "startedAt,desc",
    syncType,
    status,
  });

  function resetFilters() {
    setSyncType(undefined);
    setStatus(undefined);
    setPage(0);
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4">
        <Badge className="w-fit" variant="secondary">
          Sincronização
        </Badge>
        <div className="flex items-start gap-3">
          <RadioTower
            className="mt-1 size-6 text-muted-foreground"
            aria-hidden="true"
          />
          <div>
            <h1 className="text-3xl font-semibold tracking-normal">
              Sincronização
            </h1>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
              Monitore execuções, falhas e configuração operacional dos syncs.
              Comandos manuais ficam fora desta tela neste passo.
            </p>
          </div>
        </div>
      </section>

      <SyncStatusCards
        data={syncStatusQuery.data}
        error={syncStatusQuery.isError}
        loading={syncStatusQuery.isLoading}
      />

      <SyncExecutionsTable
        data={
          latestExecutionsQuery.data
            ? pageFromExecutions(latestExecutionsQuery.data)
            : undefined
        }
        description="Última execução registrada para cada tipo de sincronização."
        error={latestExecutionsQuery.isError}
        loading={latestExecutionsQuery.isLoading}
        onRetry={() => void latestExecutionsQuery.refetch()}
        page={0}
        title="Últimas execuções"
      />

      <SyncFilters
        onReset={resetFilters}
        onStatusChange={(value) => {
          setStatus(value);
          setPage(0);
        }}
        onSyncTypeChange={(value) => {
          setSyncType(value);
          setPage(0);
        }}
        status={status}
        syncType={syncType}
      />

      <SyncExecutionsTable
        data={executionsQuery.data}
        description="Histórico paginado das execuções de sincronização."
        error={executionsQuery.isError}
        loading={executionsQuery.isLoading}
        onPageChange={setPage}
        onRetry={() => void executionsQuery.refetch()}
        page={page}
        paginated
        title="Histórico de execuções"
      />
    </div>
  );
}
