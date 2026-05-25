"use client";

import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

type CoordinatesDialogProps = {
  name?: string;
  coordinatesJson?: string;
  open: boolean;
  onOpenChange: (open: boolean) => void;
};

export function CoordinatesDialog({
  name,
  coordinatesJson,
  open,
  onOpenChange,
}: CoordinatesDialogProps) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>Coordenadas</DialogTitle>
          <DialogDescription>
            JSON bruto da geofence {name ? `"${name}"` : "selecionada"}.
          </DialogDescription>
        </DialogHeader>
        <pre className="max-h-96 overflow-auto rounded-md bg-muted p-4 text-xs leading-5">
          {coordinatesJson}
        </pre>
        <DialogFooter>
          <Button onClick={() => onOpenChange(false)} type="button">
            Fechar
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
