import { MapPinned } from "lucide-react";

import { Badge } from "@/components/ui/badge";

export default function CheckInsPage() {
  return (
    <section className="flex flex-col gap-4">
      <Badge className="w-fit" variant="secondary">
        Check-ins
      </Badge>
      <div className="flex items-start gap-3">
        <MapPinned className="mt-1 size-6 text-muted-foreground" aria-hidden="true" />
        <div>
          <h1 className="text-3xl font-semibold tracking-normal">Check-ins</h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Consulta e registro manual de check-ins serão implementados no
            próximo passo.
          </p>
        </div>
      </div>
    </section>
  );
}
