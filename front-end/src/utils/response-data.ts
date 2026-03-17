export interface SingleResponse<T> {
  data: T;
  code: number;
  success: boolean;
  message: string;
  errors: unknown[];
  timestamp: string;
}

export interface BaseResponse<T> {
  data: T[];
  code: number;
  success: boolean;
  message: string;
  errors: unknown[];
  timestamp: string;
  pagination?: Pagination;
}

export interface Pagination {
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}
