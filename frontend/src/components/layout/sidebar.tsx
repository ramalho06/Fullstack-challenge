"use client";

import {
  Activity,
  Gauge,
  LandPlot,
  Map,
  MapPinned,
  RadioTower,
  UsersRound,
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";

import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const navigationItems = [
  {
    label: "Dashboard",
    href: "/",
    icon: Gauge,
  },
  {
    label: "Agentes",
    href: "/agents",
    icon: UsersRound,
  },
  {
    label: "Mapa",
    href: "/map",
    icon: Map,
  },
  {
    label: "Check-ins",
    href: "/check-ins",
    icon: MapPinned,
  },
  {
    label: "Geofences",
    href: "/geofences",
    icon: LandPlot,
  },
  {
    label: "Sincronização",
    href: "/sync",
    icon: RadioTower,
  },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="border-border/80 bg-sidebar text-sidebar-foreground md:border-r">
      <div className="sticky top-0 flex min-h-screen flex-col gap-8 px-4 py-5">
        <Link className="flex items-center gap-3 px-2" href="/">
          <span className="flex size-10 items-center justify-center rounded-md bg-primary text-primary-foreground">
            <Activity className="size-5" aria-hidden="true" />
          </span>
          <span className="min-w-0">
            <span className="block text-sm font-semibold leading-5">
              Teams Tracking
            </span>
            <span className="block text-xs text-muted-foreground">
              Operações externas
            </span>
          </span>
        </Link>

        <nav className="flex flex-col gap-1">
          {navigationItems.map((item) => {
            const active =
              item.href === "/" ? pathname === "/" : pathname.startsWith(item.href);
            const Icon = item.icon;

            return (
              <Link
                className={cn(
                  "flex h-10 items-center gap-3 rounded-md px-3 text-sm font-medium text-muted-foreground transition-colors hover:bg-sidebar-accent hover:text-sidebar-accent-foreground",
                  active &&
                    "bg-sidebar-accent text-sidebar-accent-foreground shadow-sm",
                )}
                href={item.href}
                key={item.href}
              >
                <Icon className="size-4" aria-hidden="true" />
                {item.label}
              </Link>
            );
          })}
        </nav>

        <div className="mt-auto rounded-md border bg-card p-4">
          <div className="mb-2 flex items-center justify-between gap-3">
            <p className="text-sm font-medium">Frontend</p>
            <Badge variant="secondary">Setup</Badge>
          </div>
          <p className="text-xs leading-5 text-muted-foreground">
            Base pronta para conectar dashboards, tabelas e formulários ao
            backend.
          </p>
        </div>
      </div>
    </aside>
  );
}
