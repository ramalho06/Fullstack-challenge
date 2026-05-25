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
  formatPercent,
  formatSpeed,
} from "@/lib/formatters";
import type { CurrentLocation } from "@/types/location";

import { DashboardError } from "./dashboard-error";
import { AgentStatusBadge } from "./status-badges";

type CurrentLocationsTableProps = {
  locations?: CurrentLocation[];
  loading?: boolean;
  error?: boolean;
};

export function CurrentLocationsTable({
  locations = [],
  loading,
  error,
}: CurrentLocationsTableProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Localizações atuais</CardTitle>
        <CardDescription>
          Últimos pontos conhecidos dos agentes com dados operacionais.
        </CardDescription>
      </CardHeader>
      <CardContent>
        {error ? (
          <DashboardError message="Não foi possível carregar as localizações atuais." />
        ) : null}

        {!error && loading ? <LocationsTableSkeleton /> : null}

        {!error && !loading && locations.length === 0 ? (
          <p className="rounded-md border bg-muted/30 px-4 py-6 text-center text-sm text-muted-foreground">
            Nenhum agente com localização atual encontrada.
          </p>
        ) : null}

        {!error && !loading && locations.length > 0 ? (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Agente</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Bateria</TableHead>
                <TableHead>Velocidade</TableHead>
                <TableHead>Endereço</TableHead>
                <TableHead>Última atualização</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {locations.slice(0, 10).map((location) => (
                <TableRow key={location.agentId}>
                  <TableCell>
                    <div className="font-medium">{location.name}</div>
                    <div className="text-xs text-muted-foreground">
                      {location.agentId}
                    </div>
                  </TableCell>
                  <TableCell>
                    <AgentStatusBadge status={location.status} />
                  </TableCell>
                  <TableCell>{formatPercent(location.battery)}</TableCell>
                  <TableCell>{formatSpeed(location.speed)}</TableCell>
                  <TableCell className="max-w-80 truncate">
                    {location.currentAddress ?? "Sem endereço"}
                  </TableCell>
                  <TableCell>
                    {location.lastSeen
                      ? formatDateTime(location.lastSeen)
                      : "Sem atualização"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : null}

        {!error && !loading && locations.length > 10 ? (
          <p className="mt-3 text-xs text-muted-foreground">
            Exibindo 10 de {locations.length} localizações.
          </p>
        ) : null}
      </CardContent>
    </Card>
  );
}

function LocationsTableSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 5 }).map((_, index) => (
        <Skeleton className="h-10 w-full" key={index} />
      ))}
    </div>
  );
}
