"use client";

import { Button } from "@/components/ui/button";

type SimplePaginationProps = {
  page: number;
  first?: boolean;
  last?: boolean;
  disabled?: boolean;
  onPageChange: (page: number) => void;
};

export function SimplePagination({
  page,
  first,
  last,
  disabled,
  onPageChange,
}: SimplePaginationProps) {
  return (
    <div className="flex flex-col gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
      <p className="text-sm text-muted-foreground">Página {page + 1}</p>
      <div className="flex gap-2">
        <Button
          disabled={disabled || first || page <= 0}
          onClick={() => onPageChange(Math.max(page - 1, 0))}
          type="button"
          variant="outline"
        >
          Anterior
        </Button>
        <Button
          disabled={disabled || last}
          onClick={() => onPageChange(page + 1)}
          type="button"
          variant="outline"
        >
          Próxima
        </Button>
      </div>
    </div>
  );
}
