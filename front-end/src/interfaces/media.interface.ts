import type { AccountResponse } from "./account.interface";

export interface MediaResponse {
  id: number;
  fileName: string;
  originalName: string;
  fileUrl: string;
  mimeType: string;
  fileSize: number;
  altText: string | null;
  caption: string | null;
  uploader: AccountResponse | null;
  createdAt: string;
  isDeleted: boolean;
  deletedAt: string | null;
  deduplicated?: boolean; // Metadata for FE UX
}
