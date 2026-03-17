import { createAsyncThunk } from "@reduxjs/toolkit";
import {
  createArticle,
  deleteArticle,
  getAllArticles,
  getArticleByIdOrSlug,
  updateArticle,
  submitForReview,
  approveArticle,
  rejectArticle,
  requestRevision,
  publishArticle
} from "@/apis/article.apis";
import { handleAxiosError } from "@/apis/error.apis";
import type {
  ArticleRequest,
  ArticleSearchRequest,
  ArticleListItem,
  ArticleDetail
} from "@/interfaces/article.interface";
import type { ErrorResponse } from "@/utils/error-response";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";

export const getAllArticlesThunk = createAsyncThunk<
  BaseResponse<ArticleListItem>,
  ArticleSearchRequest,
  { rejectValue: ErrorResponse }
>("articles/getAll", async (params, { rejectWithValue }) => {
  try {
    return await getAllArticles(params);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const getArticleDetailThunk = createAsyncThunk<
  SingleResponse<ArticleDetail>,
  string | number,
  { rejectValue: ErrorResponse }
>("articles/getDetail", async (idOrSlug, { rejectWithValue }) => {
  try {
    return await getArticleByIdOrSlug(idOrSlug);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const createArticleThunk = createAsyncThunk<
  SingleResponse<ArticleListItem>,
  ArticleRequest,
  { rejectValue: ErrorResponse }
>("articles/create", async (request, { rejectWithValue }) => {
  try {
    return await createArticle(request);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const updateArticleThunk = createAsyncThunk<
  SingleResponse<ArticleListItem>,
  { id: number; request: ArticleRequest },
  { rejectValue: ErrorResponse }
>("articles/update", async ({ id, request }, { rejectWithValue }) => {
  try {
    return await updateArticle(id, request);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const deleteArticleThunk = createAsyncThunk<
  SingleResponse<string>,
  number,
  { rejectValue: ErrorResponse }
>("articles/delete", async (id, { rejectWithValue }) => {
  try {
    return await deleteArticle(id);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

// Lifecycle thunks
export const submitForReviewThunk = createAsyncThunk<
  SingleResponse<ArticleListItem>,
  number,
  { rejectValue: ErrorResponse }
>("articles/submit", async (id, { rejectWithValue }) => {
  try {
    return await submitForReview(id);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const approveArticleThunk = createAsyncThunk<
  SingleResponse<ArticleListItem>,
  number,
  { rejectValue: ErrorResponse }
>("articles/approve", async (id, { rejectWithValue }) => {
  try {
    return await approveArticle(id);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const rejectArticleThunk = createAsyncThunk<
  SingleResponse<ArticleListItem>,
  { id: number; reason: string },
  { rejectValue: ErrorResponse }
>("articles/reject", async ({ id, reason }, { rejectWithValue }) => {
  try {
    return await rejectArticle(id, reason);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const publishArticleThunk = createAsyncThunk<
  SingleResponse<ArticleListItem>,
  number,
  { rejectValue: ErrorResponse }
>("articles/publish", async (id, { rejectWithValue }) => {
  try {
    return await publishArticle(id);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const requestRevisionThunk = createAsyncThunk<
  SingleResponse<ArticleListItem>,
  { id: number; reason: string },
  { rejectValue: ErrorResponse }
>("articles/requestRevision", async ({ id, reason }, { rejectWithValue }) => {
  try {
    return await requestRevision(id, reason);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});
