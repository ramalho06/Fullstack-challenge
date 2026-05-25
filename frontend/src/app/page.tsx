import { Activity, MapPinned, RadioTower, UsersRound } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

const dashboardCards = [
  {
    title: "Agentes",
    description: "Resumo operacional e listagem serão conectados ao backend.",
    icon: UsersRound,
  },
  {
    title: "Localizações",
    description: "Estado atual dos agentes e mapa entram nos próximos passos.",
    icon: MapPinned,
  },
  {
    title: "Sincronizações",
    description: "Monitoramento de execuções usará os endpoints já existentes.",
    icon: RadioTower,
  },
];

export default function DashboardPage() {
  return (
    <>
      <section className="flex flex-col gap-3">
        <Badge className="w-fit" variant="secondary">
          Setup inicial
        </Badge>
        <div className="flex flex-col gap-2">
          <h1 className="text-3xl font-semibold tracking-normal">Dashboard</h1>
          <p className="max-w-2xl text-sm leading-6 text-muted-foreground">
            Base visual do frontend criada. As integrações com dados reais serão
            implementadas a partir dos próximos passos.
          </p>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-3">
        {dashboardCards.map((card) => {
          const Icon = card.icon;

          return (
            <Card key={card.title}>
              <CardHeader>
                <div className="mb-3 flex size-10 items-center justify-center rounded-md bg-secondary">
                  <Icon className="size-5" aria-hidden="true" />
                </div>
                <CardTitle>{card.title}</CardTitle>
                <CardDescription>{card.description}</CardDescription>
              </CardHeader>
            </Card>
          );
        })}
      </section>

      <section className="rounded-md border bg-card p-5">
        <div className="flex items-center gap-3">
          <Activity className="size-5 text-muted-foreground" aria-hidden="true" />
          <div>
            <h2 className="text-base font-semibold">Próximo passo</h2>
            <p className="text-sm leading-6 text-muted-foreground">
              Conectar listagem de agentes, check-ins, geofences e status de
              sincronização usando TanStack Query.
            </p>
          </div>
        </div>
      </section>
    </>
  );
}
