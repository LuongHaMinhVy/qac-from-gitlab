import { createSlice } from "@reduxjs/toolkit";
import type { PayloadAction } from "@reduxjs/toolkit";
import type { ArticleListItem, ArticleDetail } from "@/interfaces/article.interface";
import {
  getAllArticlesThunk,
  getArticleDetailThunk,
  createArticleThunk,
  updateArticleThunk,
  deleteArticleThunk,
  submitForReviewThunk,
  approveArticleThunk,
  rejectArticleThunk,
  publishArticleThunk
} from "../thunks/article.thunk";
import type { BaseResponse } from "@/utils/response-data";

interface ArticleState {
  articles: ArticleListItem[];
  currentArticle: ArticleDetail | null;
  loading: boolean;
  error: string | null;
  pagination: BaseResponse<ArticleListItem>["pagination"] | null;
}

const initialState: ArticleState = {
  articles: [],
  currentArticle: null,
  loading: false,
  error: null,
  pagination: null,
};

const articleSlice = createSlice({
  name: "articles",
  initialState,
  reducers: {
    clearCurrentArticle: (state) => {
      state.currentArticle = null;
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getAllArticlesThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getAllArticlesThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.articles = action.payload.data;
        state.pagination = action.payload.pagination;
      })
      .addCase(getAllArticlesThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload?.message || "Failed to fetch articles";
      })
      .addCase(getArticleDetailThunk.pending, (state) => {
        state.loading = true;
      })
      .addCase(getArticleDetailThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.currentArticle = action.payload.data;
      })
      .addCase(getArticleDetailThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload?.message || "Failed to fetch article detail";
      })
      .addCase(deleteArticleThunk.fulfilled, (state, action) => {
        const id = action.meta.arg;
        state.articles = state.articles.filter((a) => a.id !== id);
        if (state.currentArticle?.id === id) {
          state.currentArticle = null;
        }
      })
      .addMatcher(
        (action) =>
          [
            createArticleThunk.fulfilled.type,
            updateArticleThunk.fulfilled.type,
            submitForReviewThunk.fulfilled.type,
            approveArticleThunk.fulfilled.type,
            rejectArticleThunk.fulfilled.type,
            publishArticleThunk.fulfilled.type,
          ].includes(action.type),
        (state, action: PayloadAction<any>) => {
          state.loading = false;
          const updatedArticle = action.payload.data;
          const index = state.articles.findIndex((a) => a.id === updatedArticle.id);
          if (index !== -1) {
            state.articles[index] = updatedArticle;
          }
          if (state.currentArticle?.id === updatedArticle.id) {
            state.currentArticle = { ...state.currentArticle, ...updatedArticle };
          }
        }
      );
  },
});

export const { clearCurrentArticle, clearError } = articleSlice.actions;
export default articleSlice.reducer;
