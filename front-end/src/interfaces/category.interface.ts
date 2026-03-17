import type { MediaResponse } from "./media.interface";

export interface CategoryResponse {
  id: number;
  name: string;
  slug: string;
  description: string;
  coverImage: MediaResponse | null;
  displayOrder: number;
  status: boolean;
  createdAt: string;
}

export interface CategoryRequest {
  name: string;
  description?: string;
  mediaId?: number | null;
  displayOrder?: number | null;
  status?: boolean;
}

export interface CategorySearchRequest {
  search?: string;
  page: number;
  size: number;
  sort: string;
  direction: "asc" | "desc";
}
