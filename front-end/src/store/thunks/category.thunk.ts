import {
  createCategory,
  deleteCategory,
  getAllCategories,
  updateCategory,
} from "@/apis/category.apis";
import { handleAxiosError } from "@/apis/error.apis";
import type {
  CategoryRequest,
  CategoryResponse,
  CategorySearchRequest,
} from "@/interfaces/category.interface";
import type { ErrorResponse } from "@/utils/error-response";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";
import { createAsyncThunk } from "@reduxjs/toolkit";

export const getAllCategoriesThunk = createAsyncThunk<
  BaseResponse<CategoryResponse>,
  CategorySearchRequest,
  { rejectValue: ErrorResponse }
>("categories/getAll", async (searchRequest, { rejectWithValue }) => {
  try {
    return await getAllCategories(searchRequest);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const createCategoryThunk = createAsyncThunk<
  SingleResponse<CategoryResponse>,
  { request: CategoryRequest; file?: File },
  { rejectValue: ErrorResponse }
>("categories/create", async ({ request, file }, { rejectWithValue }) => {
  try {
    return await createCategory(request, file);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const updateCategoryThunk = createAsyncThunk<
  SingleResponse<CategoryResponse>,
  { id: number; request: CategoryRequest; file?: File },
  { rejectValue: ErrorResponse }
>("categories/update", async ({ id, request, file }, { rejectWithValue }) => {
  try {
    return await updateCategory(id, request, file);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const deleteCategoryThunk = createAsyncThunk<
  SingleResponse<string>,
  { id: number; deleteArticles: boolean },
  { rejectValue: ErrorResponse }
>("categories/delete", async ({ id, deleteArticles }, { rejectWithValue }) => {
  try {
    return await deleteCategory(id, deleteArticles);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});
