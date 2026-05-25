import { UsersRound } from "lucide-react";

import { Badge } from "@/components/ui/badge";

export default function AgentsPage() {
  return (
    <section className="flex flex-col gap-4">
      <Badge className="w-fit" variant="secondary">
        Agentes
      </Badge>
      <div className="flex items-start gap-3">
        <UsersRound className="mt-1 size-6 text-muted-foreground" aria-hidden="true" />
        <div>
          <h1 className="text-3xl font-semibold tracking-normal">Agentes</h1>
          <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
            Listagem de agentes será implementada no próximo passo.
          </p>
        </div>
      </div>
    </section>
  );
}
