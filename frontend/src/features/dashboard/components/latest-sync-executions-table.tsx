import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { formatDateTime, formatNumber } from "@/lib/formatters";
import type { SyncExecution } from "@/types/sync";

import { DashboardError } from "./dashboard-error";
import { SyncStatusBadge } from "./status-badges";

type LatestSyncExecutionsTableProps = {
  executions?: SyncExecution[];
  loading?: boolean;
  error?: boolean;
};

export function LatestSyncExecutionsTable({
  executions = [],
  loading,
  error,
}: LatestSyncExecutionsTableProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Últimas sincronizações</CardTitle>
        <CardDescription>
          Última execução registrada para cada rotina automática/manual.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {error ? (
          <DashboardError message="Não foi possível carregar as últimas sincronizações." />
        ) : null}

        {!error && loading ? <SyncTableSkeleton /> : null}

        {!error && !loading && executions.length === 0 ? (
          <p className="rounded-md border bg-muted/30 px-4 py-6 text-center text-sm text-muted-foreground">
            Nenhuma execução registrada ainda.
          </p>
        ) : null}

        {!error && !loading && executions.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Tipo</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Início</TableHead>
                <TableHead>Fim</TableHead>
                <TableHead>Processados</TableHead>
                <TableHead>Criados</TableHead>
                <TableHead>Atualizados</TableHead>
                <TableHead>Ignorados</TableHead>
                <TableHead>Erro</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {executions.map((execution) => (
                <TableRow key={`${execution.syncType}-${execution.id}`}>
                  <TableCell className="font-medium">
                    {formatSyncType(execution.syncType)}
                  </TableCell>
                  <TableCell>
                    <SyncStatusBadge status={execution.status} />
                  </TableCell>
                  <TableCell>{formatDateTime(execution.startedAt)}</TableCell>
                  <TableCell>{formatDateTime(execution.finishedAt)}</TableCell>
                  <TableCell>{formatNumber(execution.itemsProcessed)}</TableCell>
                  <TableCell>{formatNumber(execution.itemsCreated)}</TableCell>
                  <TableCell>{formatNumber(execution.itemsUpdated)}</TableCell>
                  <TableCell>{formatNumber(execution.itemsSkipped)}</TableCell>
                  <TableCell className="max-w-56 truncate text-muted-foreground">
                    {execution.errorMessage ?? "—"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : null}
      </CardContent>
    </Card>
  );
}

function SyncTableSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 4 }).map((_, index) => (
        <Skeleton className="h-10 w-full" key={index} />
      ))}
    </div>
  );
}

function formatSyncType(syncType: string) {
  return syncType
    .replaceAll("_", " ")
    .toLowerCase()
    .replace(/(^|\s)\S/g, (letter) => letter.toUpperCase());
}
