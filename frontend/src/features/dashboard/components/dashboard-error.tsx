type DashboardErrorProps = {
  message?: string;
};

export function DashboardError({
  message = "Não foi possível carregar os dados do dashboard.",
}: DashboardErrorProps) {
  return (
    <div className="rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
      {message}
    </div>
  );
}
