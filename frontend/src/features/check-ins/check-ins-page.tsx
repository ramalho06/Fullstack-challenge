"use client";

import { MapPinned, Plus } from "lucide-react";
import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useAgents } from "@/features/agents/hooks/use-agents";
import type { CheckInSource, CheckInType, ManualCheckInPayload } from "@/types/check-in";

import { CheckInFilters } from "./components/check-in-filters";
import { CheckInFormDialog } from "./components/check-in-form-dialog";
import { CheckInsTable } from "./components/check-ins-table";
import { useCheckIns } from "./hooks/use-check-ins";
import { useCreateManualCheckIn } from "./hooks/use-create-manual-check-in";

const pageSize = 10;

export function CheckInsPage() {
  const [page, setPage] = useState(0);
  const [agentId, setAgentId] = useState<string | undefined>();
  const [type, setType] = useState<CheckInType | undefined>();
  const [source, setSource] = useState<CheckInSource | undefined>();
  const [dialogOpen, setDialogOpen] = useState(false);

  const agentsQuery = useAgents({ page: 0, size: 100 });
  const checkInsQuery = useCheckIns({
    page,
    size: pageSize,
    agentId,
    type,
    source,
  });
  const createManualCheckIn = useCreateManualCheckIn();

  const agents = agentsQuery.data?.content ?? [];

  function handleSubmit(payload: ManualCheckInPayload) {
    createManualCheckIn.mutate(payload, {
      onSuccess: () => {
        setDialogOpen(false);
      },
    });
  }

  function resetFilters() {
    setAgentId(undefined);
    setType(undefined);
    setSource(undefined);
    setPage(0);
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-col gap-3">
            <Badge className="w-fit" variant="secondary">
              Check-ins
            </Badge>
            <div className="flex items-start gap-3">
              <MapPinned
                className="mt-1 size-6 text-muted-foreground"
                aria-hidden="true"
              />
              <div>
                <h1 className="text-3xl font-semibold tracking-normal">
                  Check-ins
                </h1>
                <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
                  Consulte eventos sincronizados e registre check-ins manuais
                  usando agentes reais do backend.
                </p>
              </div>
            </div>
          </div>

          <Button onClick={() => setDialogOpen(true)} type="button">
            <Plus aria-hidden="true" />
            Novo check-in
          </Button>
        </div>
      </section>

      <CheckInFilters
        agentId={agentId}
        agents={agents}
        onAgentIdChange={(value) => {
          setAgentId(value);
          setPage(0);
        }}
        onReset={resetFilters}
        onSourceChange={(value) => {
          setSource(value);
          setPage(0);
        }}
        onTypeChange={(value) => {
          setType(value);
          setPage(0);
        }}
        source={source}
        type={type}
      />

      <CheckInsTable
        agents={agents}
        data={checkInsQuery.data}
        error={checkInsQuery.isError}
        loading={checkInsQuery.isLoading}
        onPageChange={setPage}
        onRetry={() => void checkInsQuery.refetch()}
        page={page}
      />

      <CheckInFormDialog
        agents={agents}
        onOpenChange={setDialogOpen}
        onSubmit={handleSubmit}
        open={dialogOpen}
        submitting={createManualCheckIn.isPending}
      />
    </div>
  );
}
