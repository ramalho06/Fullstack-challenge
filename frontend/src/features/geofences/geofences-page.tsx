"use client";

import { LandPlot } from "lucide-react";
import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import type { Geofence, GeofenceType } from "@/types/geofence";

import { CoordinatesDialog } from "./components/coordinates-dialog";
import { GeofenceFilters } from "./components/geofence-filters";
import { GeofencesTable } from "./components/geofences-table";
import { useGeofences } from "./hooks/use-geofences";

const pageSize = 10;

export function GeofencesPage() {
  const [page, setPage] = useState(0);
  const [type, setType] = useState<GeofenceType | undefined>();
  const [selectedGeofence, setSelectedGeofence] = useState<Geofence | null>(
    null,
  );

  const geofencesQuery = useGeofences({ page, size: pageSize, type });

  function resetFilters() {
    setType(undefined);
    setPage(0);
  }

  return (
    <div className="flex flex-col gap-6">
      <section className="flex flex-col gap-4">
        <Badge className="w-fit" variant="secondary">
          Geofences
        </Badge>
        <div className="flex items-start gap-3">
          <LandPlot
            className="mt-1 size-6 text-muted-foreground"
            aria-hidden="true"
          />
          <div>
            <h1 className="text-3xl font-semibold tracking-normal">
              Geofences
            </h1>
            <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
              Consulte áreas sincronizadas sem alterar geometria, equipes ou
              regras espaciais.
            </p>
          </div>
        </div>
      </section>

      <GeofenceFilters
        onReset={resetFilters}
        onTypeChange={(value) => {
          setType(value);
          setPage(0);
        }}
        type={type}
      />

      <GeofencesTable
        data={geofencesQuery.data}
        error={geofencesQuery.isError}
        loading={geofencesQuery.isLoading}
        onPageChange={setPage}
        onRetry={() => void geofencesQuery.refetch()}
        onViewCoordinates={setSelectedGeofence}
        page={page}
      />

      <CoordinatesDialog
        coordinatesJson={selectedGeofence?.coordinatesJson}
        name={selectedGeofence?.name}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedGeofence(null);
          }
        }}
        open={!!selectedGeofence}
      />
    </div>
  );
}
