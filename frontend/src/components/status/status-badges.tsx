import { Badge } from "@/components/ui/badge";
import type { AgentStatus } from "@/types/agent";
import type { OperationalSyncStatus, SyncStatus } from "@/types/sync";

const agentStatusLabels: Record<AgentStatus, string> = {
  ONLINE: "Online",
  PAUSED: "Pausado",
  SIGNAL_LOST: "Sem sinal",
  OFFLINE: "Offline",
};

const syncStatusLabels: Record<SyncStatus, string> = {
  RUNNING: "Rodando",
  SUCCESS: "Sucesso",
  FAILED: "Falha",
  PARTIAL_SUCCESS: "Parcial",
};

const operationalStatusLabels: Record<OperationalSyncStatus, string> = {
  HEALTHY: "Saudável",
  WARNING: "Atenção",
  DEGRADED: "Degradado",
};

const agentStatusClasses: Record<AgentStatus, string> = {
  ONLINE: "border-emerald-200 bg-emerald-50 text-emerald-700",
  PAUSED: "border-amber-200 bg-amber-50 text-amber-700",
  SIGNAL_LOST: "border-orange-200 bg-orange-50 text-orange-700",
  OFFLINE: "border-zinc-200 bg-zinc-100 text-zinc-600",
};

const syncStatusClasses: Record<SyncStatus, string> = {
  SUCCESS: "border-emerald-200 bg-emerald-50 text-emerald-700",
  FAILED: "border-red-200 bg-red-50 text-red-700",
  PARTIAL_SUCCESS: "border-amber-200 bg-amber-50 text-amber-700",
  RUNNING: "border-sky-200 bg-sky-50 text-sky-700",
};

const operationalStatusClasses: Record<OperationalSyncStatus, string> = {
  HEALTHY: "border-emerald-200 bg-emerald-50 text-emerald-700",
  WARNING: "border-amber-200 bg-amber-50 text-amber-700",
  DEGRADED: "border-red-200 bg-red-50 text-red-700",
};

export function AgentStatusBadge({ status }: { status: AgentStatus }) {
  return (
    <Badge className={agentStatusClasses[status]} variant="outline">
      {agentStatusLabels[status]}
    </Badge>
  );
}

export function SyncStatusBadge({ status }: { status: SyncStatus }) {
  return (
    <Badge className={syncStatusClasses[status]} variant="outline">
      {syncStatusLabels[status]}
    </Badge>
  );
}

export function OperationalStatusBadge({
  status,
}: {
  status: OperationalSyncStatus;
}) {
  return (
    <Badge className={operationalStatusClasses[status]} variant="outline">
      {operationalStatusLabels[status]}
    </Badge>
  );
}
