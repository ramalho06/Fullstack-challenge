export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
};

export type ApiErrorResponse = {
  error: {
    code: string;
    message: string;
    details: string | null;
  };
};
