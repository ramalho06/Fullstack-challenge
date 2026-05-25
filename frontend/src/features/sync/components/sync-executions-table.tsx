"use client";

import { SimplePagination } from "@/components/pagination/simple-pagination";
import { SyncStatusBadge } from "@/components/status/status-badges";
import { Button } from "@/components/ui/button";
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
import { formatDateTime, formatNumber, formatOptional } from "@/lib/formatters";
import type { PageResponse } from "@/types/api";
import type { SyncExecution } from "@/types/sync";

type SyncExecutionsTableProps = {
  data?: PageResponse<SyncExecution>;
  loading?: boolean;
  error?: boolean;
  page: number;
  title: string;
  description: string;
  paginated?: boolean;
  onPageChange?: (page: number) => void;
  onRetry: () => void;
};

export function SyncExecutionsTable({
  data,
  loading,
  error,
  page,
  title,
  description,
  paginated,
  onPageChange,
  onRetry,
}: SyncExecutionsTableProps) {
  const executions = data?.content ?? [];

  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
        <CardDescription>{description}</CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4">
        {error ? (
          <div className="rounded-md border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            <p>Não foi possível carregar execuções.</p>
            <Button
              className="mt-3"
              onClick={onRetry}
              type="button"
              variant="outline"
            >
              Tentar novamente
            </Button>
          </div>
        ) : null}

        {!error && loading ? <ExecutionsTableSkeleton /> : null}

        {!error && !loading && executions.length === 0 ? (
          <p className="rounded-md border bg-muted/30 px-4 py-8 text-center text-sm text-muted-foreground">
            Nenhuma execução encontrada.
          </p>
        ) : null}

        {!error && !loading && executions.length > 0 ? (
          <div className="overflow-x-auto">
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
                  <TableHead>HTTP</TableHead>
                  <TableHead>Erro</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {executions.map((execution) => (
                  <TableRow key={execution.id}>
                    <TableCell className="font-medium">
                      {execution.syncType}
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
                    <TableCell>{formatOptional(execution.httpStatus)}</TableCell>
                    <TableCell className="max-w-64 truncate text-muted-foreground">
                      {formatOptional(execution.errorMessage)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        ) : null}

        {paginated && onPageChange ? (
          <SimplePagination
            disabled={loading}
            first={data?.first}
            last={data?.last}
            onPageChange={onPageChange}
            page={page}
          />
        ) : null}
      </CardContent>
    </Card>
  );
}

export function pageFromExecutions(executions: SyncExecution[]) {
  return {
    content: executions,
    page: 0,
    size: executions.length,
    totalElements: executions.length,
    totalPages: executions.length > 0 ? 1 : 0,
    first: true,
    last: true,
    empty: executions.length === 0,
  } satisfies PageResponse<SyncExecution>;
}

function ExecutionsTableSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 5 }).map((_, index) => (
        <Skeleton className="h-11 w-full" key={index} />
      ))}
    </div>
  );
}
