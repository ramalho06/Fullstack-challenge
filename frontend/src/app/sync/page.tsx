import { RadioTower } from "lucide-react";

import { Badge } from "@/components/ui/badge";

export default function SyncPage() {
  return (
    <section className="flex flex-col gap-4">
      <Badge className="w-fit" variant="secondary">
        Sincronização
      </Badge>
      <div className="flex items-start gap-3">
        <RadioTower className="mt-1 size-6 text-muted-foreground" aria-hidden="true" />
        <div>
          <h1 className="text-3xl font-semibold tracking-normal">
            Sincronização
          </h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Monitoramento operacional será conectado ao backend no próximo passo.
          </p>
        </div>
      </div>
    </section>
  );
}
