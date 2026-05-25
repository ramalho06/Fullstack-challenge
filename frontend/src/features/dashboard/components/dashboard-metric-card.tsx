import type { LucideIcon } from "lucide-react";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";

type DashboardMetricCardProps = {
  title: string;
  value: string | number;
  description: string;
  icon: LucideIcon;
  loading?: boolean;
  error?: boolean;
};

export function DashboardMetricCard({
  title,
  value,
  description,
  icon: Icon,
  loading,
  error,
}: DashboardMetricCardProps) {
  return (
    <Card>
      <CardHeader className="pb-0">
        <div className="flex items-start justify-between gap-3">
          <div>
            <CardDescription>{title}</CardDescription>
            <CardTitle className="mt-2 text-2xl font-semibold">
              {loading ? <Skeleton className="h-8 w-20" /> : value}
            </CardTitle>
          </div>
          <div className="flex size-10 items-center justify-center rounded-md bg-secondary">
            <Icon className="size-5 text-muted-foreground" aria-hidden="true" />
          </div>
        </div>
      </CardHeader>
      <CardContent>
        <p className="text-xs leading-5 text-muted-foreground">
          {error ? "Não foi possível carregar este indicador." : description}
        </p>
      </CardContent>
    </Card>
  );
}
