import { createAsyncThunk } from "@reduxjs/toolkit";
import type {
  DashboardSummaryResponse,
  DashboardChartResponse,
} from "@/interfaces/dashboard.interface";
import type { SingleResponse } from "@/utils/response-data";
import type { ErrorResponse } from "@/utils/error-response";
import {
  fetchDashBoardSummary,
  fetchDashBoardCharts,
} from "@/apis/dashboard.apis";
import { handleAxiosError } from "@/apis/error.apis";


export const getDashboardSummaryThunk = createAsyncThunk<
  DashboardSummaryResponse,
  void,
  { rejectValue: ErrorResponse }
>("dashboard/getSummary", async (_, { rejectWithValue }) => {
  try {
    const res: SingleResponse<DashboardSummaryResponse> =
      await fetchDashBoardSummary();
    return res.data;
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});


export const getDashboardChartsThunk = createAsyncThunk<
  DashboardChartResponse,
  void,
  { rejectValue: ErrorResponse }
>("dashboard/getCharts", async (_, { rejectWithValue }) => {
  try {
    const res: SingleResponse<DashboardChartResponse> = await fetchDashBoardCharts();
    return res.data;
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});
