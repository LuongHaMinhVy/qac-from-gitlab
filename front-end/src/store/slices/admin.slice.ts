import { createSlice } from "@reduxjs/toolkit";
import { 
  getAllAdminsThunk, 
  createAdminThunk, 
  updateAdminThunk, 
  deleteAdminThunk, 
  assignRolesThunk 
} from "../thunks/admin.thunk";
import type { AdminResponse } from "@/apis/admin.apis";
import type { ErrorResponse } from "@/utils/error-response";

interface AdminManagementState {
  admins: AdminResponse[];
  loading: boolean;
  actionLoading: boolean;
  error: string | null;
  pagination: {
    totalElements: number;
    totalPages: number;
    currentPage: number;
    pageSize: number;
  } | null;
}

const initialState: AdminManagementState = {
  admins: [],
  loading: false,
  actionLoading: false,
  error: null,
  pagination: null,
};

const adminSlice = createSlice({
  name: "adminManagement",
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    // List
    builder
      .addCase(getAllAdminsThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getAllAdminsThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.admins = action.payload.data;
        state.pagination = action.payload.pagination || null;
      })
      .addCase(getAllAdminsThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = (action.payload as ErrorResponse)?.status || "Failed to fetch admins";
      });

    // Create/Update/Delete/Assign Roles
    const actionThunks = [createAdminThunk, updateAdminThunk, deleteAdminThunk, assignRolesThunk];
    actionThunks.forEach(thunk => {
      builder
        .addCase(thunk.pending, (state) => {
          state.actionLoading = true;
          state.error = null;
        })
        .addCase(thunk.rejected, (state, action) => {
          state.actionLoading = false;
          state.error = (action.payload as ErrorResponse)?.status || "Action failed";
        })
        .addCase(thunk.fulfilled, (state) => {
          state.actionLoading = false;
        });
    });
  }
});

export const { clearError } = adminSlice.actions;
export default adminSlice.reducer;
