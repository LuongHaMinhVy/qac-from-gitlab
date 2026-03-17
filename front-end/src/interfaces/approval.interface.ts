import type { ArticleStatus } from "./article.interface";

export interface ApprovalResponse {
  id: number;
  articleId: number;
  articleTitle: string;
  reviewerId: number;
  reviewerName: string;
  oldStatus: ArticleStatus;
  newStatus: ArticleStatus;
  reason: string;
  createdAt: string;
}
