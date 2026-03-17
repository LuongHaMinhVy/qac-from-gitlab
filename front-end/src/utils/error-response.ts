export interface ErrorResponse {
  message: string;
  code: number;
  status: string;
  data?: unknown[];
}
