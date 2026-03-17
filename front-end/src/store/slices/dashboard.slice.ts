import { createSlice } from "@reduxjs/toolkit";
import type {
  DashboardSummaryResponse,
  DashboardChartResponse,
} from "@/interfaces/dashboard.interface";
import type { ErrorResponse } from "@/utils/error-response";
import {
  getDashboardSummaryThunk,
  getDashboardChartsThunk,
} from "@/store/thunks/dashboard.thunk";

interface DashboardState {
  summary: DashboardSummaryResponse | null;
  charts: DashboardChartResponse | null;
  loading: boolean;
  error: ErrorResponse | null;
}

const initialState: DashboardState = {
  summary: null,
  charts: null,
  loading: false,
  error: null,
};

const dashboardSlice = createSlice({
  name: "dashboard",
  initialState,
  reducers: {
    clearDashboardError(state) {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getDashboardSummaryThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getDashboardSummaryThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.summary = action.payload;
      })
      .addCase(getDashboardSummaryThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload ?? null;
      })

      .addCase(getDashboardChartsThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getDashboardChartsThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.charts = action.payload;
      })
      .addCase(getDashboardChartsThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload ?? null;
      });
  },
});

export const { clearDashboardError } = dashboardSlice.actions;
export default dashboardSlice.reducer;
