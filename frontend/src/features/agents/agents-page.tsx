"use client";

import { Plus, UsersRound } from "lucide-react";
import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import type { Agent, AgentStatus, AgentWritePayload } from "@/types/agent";

import { AgentFilters } from "./components/agent-filters";
import { AgentFormDialog } from "./components/agent-form-dialog";
import { AgentsTable } from "./components/agents-table";
import {
  useCreateAgent,
  useDeactivateAgent,
  useUpdateAgent,
} from "./hooks/use-agent-mutations";
import { useAgents } from "./hooks/use-agents";

const pageSize = 10;

export function AgentsPage() {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState("");
  const [status, setStatus] = useState<AgentStatus | undefined>();
  const [active, setActive] = useState<boolean | undefined>();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingAgent, setEditingAgent] = useState<Agent | null>(null);

  const agentsQuery = useAgents({
    page,
    size: pageSize,
    search: search || undefined,
    status,
    active,
  });
  const createAgent = useCreateAgent();
  const updateAgent = useUpdateAgent();
  const deactivateAgent = useDeactivateAgent();

  const submitting = createAgent.isPending || updateAgent.isPending;

  function handleCreateClick() {
    setEditingAgent(null);
    setDialogOpen(true);
  }

  function handleEditClick(agent: Agent) {
    setEditingAgent(agent);
    setDialogOpen(true);
  }

  function handleSubmit(payload: AgentWritePayload) {
    if (editingAgent) {
      updateAgent.mutate(
        { id: editingAgent.id, payload },
        {
          onSuccess: () => {
            setDialogOpen(false);
            setEditingAgent(null);
          },
        },
      );
      return;
    }

    createAgent.mutate(payload, {
      onSuccess: () => {
        setDialogOpen(false);
      },
    });
  }

  function handleDeactivate(agent: Agent) {
    deactivateAgent.mutate(agent.id);
  }

  function resetFilters() {
    setSearch("");
    setStatus(undefined);
    setActive(undefined);
    setPage(0);
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-col gap-3">
            <Badge className="w-fit" variant="secondary">
              Agentes
            </Badge>
            <div className="flex items-start gap-3">
              <UsersRound
                className="mt-1 size-6 text-muted-foreground"
                aria-hidden="true"
              />
              <div>
                <h1 className="text-3xl font-semibold tracking-normal">
                  Agentes
                </h1>
                <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
                  Liste, filtre, crie, edite e desative agentes sem expor campos
                  controlados pela sincronização.
                </p>
              </div>
            </div>
          </div>

          <Button onClick={handleCreateClick} type="button">
            <Plus aria-hidden="true" />
            Novo agente
          </Button>
        </div>
      </section>

      <AgentFilters
        active={active}
        onActiveChange={(value) => {
          setActive(value);
          setPage(0);
        }}
        onReset={resetFilters}
        onSearchChange={(value) => {
          setSearch(value);
          setPage(0);
        }}
        onStatusChange={(value) => {
          setStatus(value);
          setPage(0);
        }}
        search={search}
        status={status}
      />

      <AgentsTable
        data={agentsQuery.data}
        error={agentsQuery.isError}
        loading={agentsQuery.isLoading}
        onDeactivate={handleDeactivate}
        onEdit={handleEditClick}
        onPageChange={setPage}
        onRetry={() => void agentsQuery.refetch()}
        page={page}
      />

      <AgentFormDialog
        agent={editingAgent}
        onOpenChange={setDialogOpen}
        onSubmit={handleSubmit}
        open={dialogOpen}
        submitting={submitting}
      />
    </div>
  );
}
