"use client";

import { Eye } from "lucide-react";

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
  formatBoolean,
  formatDateTime,
  formatOptional,
} from "@/lib/formatters";
import type { PageResponse } from "@/types/api";
import type { Geofence } from "@/types/geofence";

type GeofencesTableProps = {
  data?: PageResponse<Geofence>;
  loading?: boolean;
  error?: boolean;
  page: number;
  onPageChange: (page: number) => void;
  onRetry: () => void;
  onViewCoordinates: (geofence: Geofence) => void;
};

export function GeofencesTable({
  data,
  loading,
  error,
  page,
  onPageChange,
  onRetry,
  onViewCoordinates,
}: GeofencesTableProps) {
  const geofences = data?.content ?? [];

  return (
    <Card>
      <CardHeader>
        <CardTitle>Geofences</CardTitle>
        <CardDescription>
          Consulta das áreas sincronizadas. O JSON de coordenadas é preservado
          bruto.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4">
        {error ? (
          <div className="rounded-md border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            <p>Não foi possível carregar geofences.</p>
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

        {!error && loading ? <GeofencesTableSkeleton /> : null}

        {!error && !loading && geofences.length === 0 ? (
          <p className="rounded-md border bg-muted/30 px-4 py-8 text-center text-sm text-muted-foreground">
            Nenhuma geofence encontrada.
          </p>
        ) : null}

        {!error && !loading && geofences.length > 0 ? (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nome</TableHead>
                  <TableHead>Tipo</TableHead>
                  <TableHead>Alertar entrada</TableHead>
                  <TableHead>Alertar saída</TableHead>
                  <TableHead>Equipes atribuídas</TableHead>
                  <TableHead>Sincronizado em</TableHead>
                  <TableHead>Coordenadas</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {geofences.map((geofence) => (
                  <TableRow key={geofence.id}>
                    <TableCell>
                      <div className="font-medium">{geofence.name}</div>
                      <div className="text-xs text-muted-foreground">
                        {geofence.externalId}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{geofence.type}</Badge>
                    </TableCell>
                    <TableCell>{formatBoolean(geofence.alertOnEnter)}</TableCell>
                    <TableCell>{formatBoolean(geofence.alertOnExit)}</TableCell>
                    <TableCell>{formatOptional(geofence.assignedTeams)}</TableCell>
                    <TableCell>{formatDateTime(geofence.syncedAt)}</TableCell>
                    <TableCell>
                      <div className="flex max-w-96 items-center gap-2">
                        <code className="truncate rounded bg-muted px-2 py-1 text-xs">
                          {geofence.coordinatesJson}
                        </code>
                        <Button
                          onClick={() => onViewCoordinates(geofence)}
                          size="sm"
                          type="button"
                          variant="outline"
                        >
                          <Eye aria-hidden="true" />
                          Ver
                        </Button>
                      </div>
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

function GeofencesTableSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 5 }).map((_, index) => (
        <Skeleton className="h-11 w-full" key={index} />
      ))}
    </div>
  );
}
