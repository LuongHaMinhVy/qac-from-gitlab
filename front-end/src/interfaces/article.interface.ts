import type { MediaResponse } from "./media.interface";

export type ArticleStatus = 'draft' | 'pending_review' | 'approved' | 'published' | 'rejected' | 'needs_revision';

export interface ArticleListItem {
  id: number;
  title: string;
  slug: string;
  excerpt: string;
  featuredImage: MediaResponse | null;
  status: ArticleStatus;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  authorName: string;
  categoryName: string;
  categoryId: number;
  publishedAt: string | null;
  createdAt: string;
  updatedAt: string;
  hashtag: string | null;
}

export interface ArticleDetail extends ArticleListItem {
  content: string;
  allowComments: boolean;
  isHighlight: boolean;
  isFeatured: boolean;
  relatedArticles?: ArticleListItem[];
}

export interface ArticleRequest {
  title: string;
  excerpt: string;
  content: string;
  categoryId: number;
  mediaId?: number | null;
  hashtag?: string | null;
  isHighlight?: boolean;
  isFeatured?: boolean;
  allowComments?: boolean;
}

export interface ArticleSearchRequest {
  search?: string;
  status?: ArticleStatus;
  categoryId?: number;
  authorId?: number;
  hashtag?: string;
  startDate?: string;
  endDate?: string;
  isHighlight?: boolean;
  isFeatured?: boolean;
  page: number;
  size: number;
  sort: "createdAt" | "publishedAt" | "viewCount" | "title";
  direction: "asc" | "desc";
}
