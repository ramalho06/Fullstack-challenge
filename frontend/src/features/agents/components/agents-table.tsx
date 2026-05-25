"use client";

import { Pencil, UserX } from "lucide-react";

import { SimplePagination } from "@/components/pagination/simple-pagination";
import { AgentStatusBadge } from "@/components/status/status-badges";
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
import { formatBoolean, formatDateTime, formatOptional } from "@/lib/formatters";
import type { Agent } from "@/types/agent";
import type { PageResponse } from "@/types/api";

type AgentsTableProps = {
  data?: PageResponse<Agent>;
  loading?: boolean;
  error?: boolean;
  page: number;
  onPageChange: (page: number) => void;
  onEdit: (agent: Agent) => void;
  onDeactivate: (agent: Agent) => void;
  onRetry: () => void;
};

export function AgentsTable({
  data,
  loading,
  error,
  page,
  onPageChange,
  onEdit,
  onDeactivate,
  onRetry,
}: AgentsTableProps) {
  const agents = data?.content ?? [];

  return (
    <Card>
      <CardHeader>
        <CardTitle>Agentes</CardTitle>
        <CardDescription>
          Gestão visual dos agentes cadastrados e sincronizados.
        </CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4">
        {error ? (
          <div className="rounded-md border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            <p>Não foi possível carregar agentes.</p>
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

        {!error && loading ? <AgentsTableSkeleton /> : null}

        {!error && !loading && agents.length === 0 ? (
          <p className="rounded-md border bg-muted/30 px-4 py-8 text-center text-sm text-muted-foreground">
            Nenhum agente encontrado.
          </p>
        ) : null}

        {!error && !loading && agents.length > 0 ? (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Nome</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Papel</TableHead>
                  <TableHead>Equipe</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Telefone</TableHead>
                  <TableHead>Ativo</TableHead>
                  <TableHead>Última atualização</TableHead>
                  <TableHead>Ações</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {agents.map((agent) => (
                  <TableRow key={agent.id}>
                    <TableCell>
                      <div className="font-medium">{agent.name}</div>
                      <div className="text-xs text-muted-foreground">
                        {agent.id}
                      </div>
                    </TableCell>
                    <TableCell>
                      <AgentStatusBadge status={agent.status} />
                    </TableCell>
                    <TableCell>{formatOptional(agent.role)}</TableCell>
                    <TableCell>{formatOptional(agent.team)}</TableCell>
                    <TableCell>{formatOptional(agent.email)}</TableCell>
                    <TableCell>{formatOptional(agent.phone)}</TableCell>
                    <TableCell>
                      <Badge variant={agent.active ? "secondary" : "outline"}>
                        {formatBoolean(agent.active)}
                      </Badge>
                    </TableCell>
                    <TableCell>{formatDateTime(agent.updatedAt)}</TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button
                          onClick={() => onEdit(agent)}
                          size="sm"
                          type="button"
                          variant="outline"
                        >
                          <Pencil aria-hidden="true" />
                          Editar
                        </Button>
                        <Button
                          disabled={!agent.active}
                          onClick={() => onDeactivate(agent)}
                          size="sm"
                          type="button"
                          variant="destructive"
                        >
                          <UserX aria-hidden="true" />
                          Desativar
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

function AgentsTableSkeleton() {
  return (
    <div className="space-y-3">
      {Array.from({ length: 6 }).map((_, index) => (
        <Skeleton className="h-11 w-full" key={index} />
      ))}
    </div>
  );
}
