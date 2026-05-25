const dateTimeFormatter = new Intl.DateTimeFormat("pt-BR", {
  day: "2-digit",
  month: "2-digit",
  year: "numeric",
  hour: "2-digit",
  minute: "2-digit",
  timeZoneName: "short",
});

const numberFormatter = new Intl.NumberFormat("pt-BR", {
  maximumFractionDigits: 2,
});

export function formatDateTime(value?: string | null) {
  if (!value) {
    return "—";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return dateTimeFormatter.format(date);
}

export function formatNumber(value?: number | null) {
  if (value === undefined || value === null) {
    return "—";
  }

  return numberFormatter.format(value);
}

export function formatPercent(value?: number | null) {
  if (value === undefined || value === null) {
    return "—";
  }

  return `${formatNumber(value)}%`;
}

export function formatSpeed(value?: number | null) {
  if (value === undefined || value === null) {
    return "—";
  }

  return `${formatNumber(value)} km/h`;
}

export function formatBoolean(value?: boolean | null) {
  if (value === undefined || value === null) {
    return "—";
  }

  return value ? "Sim" : "Não";
}

export function formatOptional(value?: string | number | null) {
  if (value === undefined || value === null || value === "") {
    return "—";
  }

  return String(value);
}
