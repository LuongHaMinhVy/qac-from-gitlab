import { axiosInstance, axiosInstanceMediaType } from "@/utils/axios-instance";
import type { ArticleListItem, ArticleDetail, ArticleRequest, ArticleSearchRequest } from "@/interfaces/article.interface";
import type { ApprovalResponse } from "@/interfaces/approval.interface";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";
import { handleAxiosError } from "./error.apis";

export const getAllArticles = async (params: ArticleSearchRequest): Promise<BaseResponse<ArticleListItem>> => {
  try {
    const res = await axiosInstance.get("/articles", { params });
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getArticleByIdOrSlug = async (idOrSlug: string | number): Promise<SingleResponse<ArticleDetail>> => {
  try {
    const res = await axiosInstance.get(`/articles/${idOrSlug}`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createArticle = async (request: ArticleRequest): Promise<SingleResponse<ArticleListItem>> => {
  try {
    const formData = new FormData();
    const blob = new Blob([JSON.stringify(request)], { type: "application/json" });
    formData.append("request", blob);
    
    // Lưu ý: Nếu có file upload trực tiếp, part sẽ là "file". 
    // Ở đây ta dùng mediaId đã upload trước đó nên "file" để trống.
    
    const res = await axiosInstanceMediaType.post("/articles", formData);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateArticle = async (id: number, request: ArticleRequest): Promise<SingleResponse<ArticleListItem>> => {
  try {
    const formData = new FormData();
    const blob = new Blob([JSON.stringify(request)], { type: "application/json" });
    formData.append("request", blob);
    
    const res = await axiosInstanceMediaType.put(`/articles/${id}`, formData);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteArticle = async (id: number): Promise<SingleResponse<string>> => {
  try {
    const res = await axiosInstance.delete(`/articles/${id}`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

// Lifecycle methods
export const submitForReview = async (id: number): Promise<SingleResponse<ArticleListItem>> => {
  try {
    const res = await axiosInstance.put(`/articles/${id}/submit`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const approveArticle = async (id: number): Promise<SingleResponse<ArticleListItem>> => {
  try {
    const res = await axiosInstance.put(`/articles/${id}/approve`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const rejectArticle = async (id: number, reason: string): Promise<SingleResponse<ArticleListItem>> => {
  try {
    const res = await axiosInstance.put(`/articles/${id}/reject`, { reason });
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const requestRevision = async (id: number, reason: string): Promise<SingleResponse<ArticleListItem>> => {
  try {
    const res = await axiosInstance.put(`/articles/${id}/request-revision`, { reason });
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const publishArticle = async (id: number): Promise<SingleResponse<ArticleListItem>> => {
  try {
    const res = await axiosInstance.put(`/articles/${id}/publish`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getReviewLogs = async (id: number): Promise<BaseResponse<ApprovalResponse>> => {
  try {
    const res = await axiosInstance.get(`/articles/${id}/review-logs`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
