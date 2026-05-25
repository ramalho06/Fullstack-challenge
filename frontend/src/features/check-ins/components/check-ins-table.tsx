"use client";

import { SimplePagination } from "@/components/pagination/simple-pagination";
import { Badge } from "@/components/ui/badge";
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
import {
  formatDateTime,
  formatNumber,
  formatOptional,
} from "@/lib/formatters";
import type { Agent } from "@/types/agent";
import type { PageResponse } from "@/types/api";
import type { CheckIn } from "@/types/check-in";

type CheckInsTableProps = {
  data?: PageResponse<CheckIn>;
  agents: Agent[];
  loading?: boolean;
  error?: boolean;
  page: number;
  onPageChange: (page: number) => void;
  onRetry: () => void;
};

export function CheckInsTable({
  data,
  agents,
  loading,
  error,
  page,
  onPageChange,
  onRetry,
}: CheckInsTableProps) {
  const checkIns = data?.content ?? [];
  const agentsById = new Map(agents.map((agent) => [agent.id, agent.name]));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Check-ins</CardTitle>
        <CardDescription>
          Consulta paginada dos eventos sincronizados e manuais.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4">
        {error ? (
          <div className="rounded-md border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            <p>Não foi possível carregar check-ins.</p>
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

        {!error && loading ? <CheckInsTableSkeleton /> : null}

        {!error && !loading && checkIns.length === 0 ? (
          <p className="rounded-md border bg-muted/30 px-4 py-8 text-center text-sm text-muted-foreground">
            Nenhum check-in encontrado.
          </p>
        ) : null}

        {!error && !loading && checkIns.length > 0 ? (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Agente</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Origem</TableHead>
                  <TableHead>Endereço</TableHead>
                  <TableHead>Latitude</TableHead>
                  <TableHead>Longitude</TableHead>
                  <TableHead>Precisão</TableHead>
                  <TableHead>Ocorrido em</TableHead>
                  <TableHead>Notas</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {checkIns.map((checkIn) => (
                  <TableRow key={checkIn.id}>
                    <TableCell>
                      <div className="font-medium">
                        {agentsById.get(checkIn.agentId) ?? checkIn.agentId}
                      </div>
                      <div className="text-xs text-muted-foreground">
                        {checkIn.agentId}
                      </div>
                    </TableCell>
                    <TableCell>{checkIn.type}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{checkIn.source}</Badge>
                    </TableCell>
                    <TableCell className="max-w-72 truncate">
                      {formatOptional(checkIn.address)}
                    </TableCell>
                    <TableCell>{formatNumber(checkIn.latitude)}</TableCell>
                    <TableCell>{formatNumber(checkIn.longitude)}</TableCell>
                    <TableCell>{formatNumber(checkIn.accuracy)}</TableCell>
                    <TableCell>{formatDateTime(checkIn.occurredAt)}</TableCell>
                    <TableCell className="max-w-64 truncate">
                      {formatOptional(checkIn.notes)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        ) : null}

        <SimplePagination
          disabled={loading}
          first={data?.first}
          last={data?.last}
          onPageChange={onPageChange}
          page={page}
        />
      </CardContent>
    </Card>
  );
}

function CheckInsTableSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 6 }).map((_, index) => (
        <Skeleton className="h-11 w-full" key={index} />
      ))}
    </div>
  );
}
