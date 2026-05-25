"use client";

import dynamic from "next/dynamic";
import { useMemo, useState } from "react";
import { Map as MapIcon } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { getTodayDateInputValue } from "@/features/map/utils/date";

import { AgentRouteControls } from "./agent-route-controls";
import { RouteSummaryCard } from "./route-summary-card";
import { useCurrentLocations } from "../hooks/use-current-locations";
import { useMapAgents } from "../hooks/use-map-agents";
import { useMapGeofences } from "../hooks/use-map-geofences";
import { useAgentRoute } from "../hooks/use-route-query";

const MapView = dynamic(() => import("./map-view"), {
  ssr: false,
  loading: () => <Skeleton className="h-[560px] w-full rounded-md" />,
});

export function MapPageContent() {
  const [selectedAgentId, setSelectedAgentId] = useState("");
  const [selectedDate, setSelectedDate] = useState(getTodayDateInputValue);
  const [showGeofences, setShowGeofences] = useState(true);
  const [routeRequest, setRouteRequest] = useState<{
    agentId: string;
    date: string;
  } | null>(null);

  const locationsQuery = useCurrentLocations();
  const agentsQuery = useMapAgents();
  const geofencesQuery = useMapGeofences();
  const routeQuery = useAgentRoute(
    routeRequest?.agentId ?? null,
    routeRequest?.date ?? null,
    Boolean(routeRequest),
  );

  const agents = useMemo(
    () => agentsQuery.data?.content ?? [],
    [agentsQuery.data?.content],
  );
  const locations = locationsQuery.data ?? [];
  const geofences = geofencesQuery.data?.content ?? [];
  const validLocations = locations.filter(
    (location) => location.latitude !== null && location.longitude !== null,
  );
  const selectedAgent = useMemo(
    () => agents.find((agent) => agent.id === routeRequest?.agentId),
    [agents, routeRequest?.agentId],
  );
  const routePoints = routeQuery.data?.points ?? [];

  function handleLoadRoute() {
    if (!selectedAgentId || !selectedDate) {
      return;
    }

    setRouteRequest({
      agentId: selectedAgentId,
      date: selectedDate,
    });
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4">
        <Badge className="w-fit" variant="secondary">
          Mapa
        </Badge>
        <div className="flex items-start gap-3">
          <MapIcon
            className="mt-1 size-6 text-muted-foreground"
            aria-hidden="true"
          />
          <div>
            <h1 className="text-3xl font-semibold tracking-normal">Mapa</h1>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
              Visualização geográfica dos agentes e rotas do dia. As
              localizações são atualizadas automaticamente a cada 30 segundos.
            </p>
          </div>
        </div>
      </section>

      <AgentRouteControls
        agents={agents}
        geofenceCount={geofences.length}
        geofencesError={geofencesQuery.isError}
        geofencesLoading={geofencesQuery.isLoading}
        loadingAgents={agentsQuery.isLoading}
        loadingRoute={routeQuery.isFetching}
        onAgentChange={setSelectedAgentId}
        onDateChange={setSelectedDate}
        onLoadRoute={handleLoadRoute}
        onShowGeofencesChange={setShowGeofences}
        selectedAgentId={selectedAgentId}
        selectedDate={selectedDate}
        showGeofences={showGeofences}
      />

      <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_320px]">
        <Card className="overflow-hidden">
          <CardHeader>
            <CardTitle>Localizações atuais</CardTitle>
            <CardDescription>
              {locationsQuery.isLoading
                ? "Carregando localizações..."
                : locationsQuery.isError
                  ? "Não foi possível carregar as localizações."
                  : validLocations.length === 0
                    ? "Nenhuma localização disponível."
                    : `${validLocations.length} agente(s) com coordenadas válidas.`}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="relative overflow-hidden rounded-md border">
              <MapView
                geofences={geofences}
                locations={locations}
                routePoints={routePoints}
                showGeofences={showGeofences}
              />
              {locationsQuery.isError ? (
                <div className="absolute top-4 left-4 rounded-md border border-destructive/30 bg-background/95 px-4 py-3 text-sm text-destructive shadow-sm">
                  Não foi possível carregar as localizações.
                </div>
              ) : null}
              {geofencesQuery.isError ? (
                <div className="absolute top-4 right-4 rounded-md border border-destructive/30 bg-background/95 px-4 py-3 text-sm text-destructive shadow-sm">
                  Não foi possível carregar geofences.
                </div>
              ) : null}
              {!geofencesQuery.isLoading &&
              !geofencesQuery.isError &&
              geofences.length === 0 ? (
                <div className="absolute top-4 right-4 rounded-md border bg-background/95 px-4 py-3 text-sm text-muted-foreground shadow-sm">
                  Nenhuma geofence disponível.
                </div>
              ) : null}
              {!locationsQuery.isLoading &&
              !locationsQuery.isError &&
              validLocations.length === 0 ? (
                <div className="absolute top-4 left-4 rounded-md border bg-background/95 px-4 py-3 text-sm text-muted-foreground shadow-sm">
                  Nenhuma localização disponível.
                </div>
              ) : null}
              {routeRequest &&
              !routeQuery.isLoading &&
              !routeQuery.isError &&
              routeQuery.data &&
              routeQuery.data.points.length === 0 ? (
                <div className="absolute right-4 bottom-4 rounded-md border bg-background/95 px-4 py-3 text-sm text-muted-foreground shadow-sm">
                  Nenhum ponto encontrado para a rota neste dia.
                </div>
              ) : null}
            </div>
          </CardContent>
        </Card>

        <RouteSummaryCard
          error={routeQuery.isError}
          loading={routeQuery.isFetching}
          requested={Boolean(routeRequest)}
          route={routeQuery.data}
          selectedAgent={selectedAgent}
          selectedDate={routeRequest?.date ?? selectedDate}
        />
      </div>
    </div>
  );
}
