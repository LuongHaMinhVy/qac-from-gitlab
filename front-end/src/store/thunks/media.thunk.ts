import { createAsyncThunk } from "@reduxjs/toolkit";
import {
  uploadMedia,
  getAllMedia,
  softDeleteMedia,
  restoreMedia
} from "@/apis/media.apis";
import { handleAxiosError } from "@/apis/error.apis";
import type { MediaResponse } from "@/interfaces/media.interface";
import type { ErrorResponse } from "@/utils/error-response";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";

export const uploadMediaThunk = createAsyncThunk<
  SingleResponse<MediaResponse>,
  File,
  { rejectValue: ErrorResponse }
>("media/upload", async (file, { rejectWithValue }) => {
  try {
    return await uploadMedia(file);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const getAllMediaThunk = createAsyncThunk<
  BaseResponse<MediaResponse>,
  { page?: number; size?: number; keyword?: string; mimeType?: string },
  { rejectValue: ErrorResponse }
>("media/getAll", async ({ page, size, keyword, mimeType }, { rejectWithValue }) => {
  try {
    return await getAllMedia(page, size, keyword, mimeType);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const deleteMediaThunk = createAsyncThunk<
  SingleResponse<string>,
  number,
  { rejectValue: ErrorResponse }
>("media/delete", async (id, { rejectWithValue }) => {
  try {
    return await softDeleteMedia(id);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const restoreMediaThunk = createAsyncThunk<
  SingleResponse<MediaResponse>,
  number,
  { rejectValue: ErrorResponse }
>("media/restore", async (id, { rejectWithValue }) => {
  try {
    return await restoreMedia(id);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});
