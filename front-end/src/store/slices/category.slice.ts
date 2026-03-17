import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { CategoryResponse } from "@/interfaces/category.interface";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";
import type { ErrorResponse } from "@/utils/error-response";
import {
  getAllCategoriesThunk,
  createCategoryThunk,
  updateCategoryThunk,
  deleteCategoryThunk,
} from "../thunks/category.thunk";

interface CategoryState {
  categories: CategoryResponse[];
  loading: boolean;
  error: ErrorResponse | null;

  pagination: {
    currentPage: number;
    pageSize: number;
    totalElements: number;
    totalPages: number;
  } | null;
}

const initialState: CategoryState = {
  categories: [],
  loading: false,
  error: null,
  pagination: null,
};

const categorySlice = createSlice({
  name: "categories",
  initialState,
  reducers: {
    clearCategoryError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getAllCategoriesThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(
        getAllCategoriesThunk.fulfilled,
        (state, action: PayloadAction<BaseResponse<CategoryResponse>>) => {
          state.loading = false;
          state.categories = action.payload.data;
          state.pagination = action.payload.pagination ?? null;
        }
      )
      .addCase(getAllCategoriesThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload ?? null;
      });

    builder
      .addCase(createCategoryThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(
        createCategoryThunk.fulfilled,
        (state, action: PayloadAction<SingleResponse<CategoryResponse>>) => {
          state.loading = false;
          state.categories.unshift(action.payload.data);
        }
      )
      .addCase(createCategoryThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload ?? null;
      });

    builder
      .addCase(updateCategoryThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(
        updateCategoryThunk.fulfilled,
        (state, action: PayloadAction<SingleResponse<CategoryResponse>>) => {
          state.loading = false;
          const index = state.categories.findIndex(
            (c) => c.id === action.payload.data.id
          );
          if (index !== -1) {
            state.categories[index] = action.payload.data;
          }
        }
      )
      .addCase(updateCategoryThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload ?? null;
      });

    builder
      .addCase(deleteCategoryThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteCategoryThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.categories = state.categories.filter(
          (c) => c.id !== action.meta.arg.id
        );

        if (state.pagination) {
          state.pagination.totalElements--;
        }
      })
      .addCase(deleteCategoryThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload ?? null;
      });
  },
});

export const { clearCategoryError } = categorySlice.actions;
export default categorySlice.reducer;
