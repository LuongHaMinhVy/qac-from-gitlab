export interface BadWordRequest {
  word: string;
  replacement?: string;
  severity: number;
  isActive: boolean;
}

export interface BadWordResponse {
  id: number;
  word: string;
  replacement: string;
  severity: number;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
}

export interface BadWordSearchRequest {
  page?: number;
  size?: number;
  sort?: string;
  direction?: "asc" | "desc";
  keyword?: string;
}
