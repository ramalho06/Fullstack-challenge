import type { Metadata } from "next";
import { AppShell } from "@/components/layout/app-shell";
import { AppProviders } from "@/components/providers/app-providers";
import "leaflet/dist/leaflet.css";
import "./globals.css";

export const metadata: Metadata = {
  title: "Teams Tracking System",
  description:
    "Frontend para rastreamento de equipes externas, check-ins e sincronizações.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR" className="h-full antialiased">
      <body className="min-h-full flex flex-col">
        <AppProviders>
          <AppShell>{children}</AppShell>
        </AppProviders>
      </body>
    </html>
  );
}
