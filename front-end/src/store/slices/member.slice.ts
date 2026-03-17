import { createSlice } from "@reduxjs/toolkit";
import {
  getMembersThunk,
  approveMemberThunk,
  rejectMemberThunk,
  updateMemberStatusThunk,
  getMemberDetailThunk,
  importMembersThunk,
  getPendingAuthorRequestsThunk,
} from "@/store/thunks/member.thunk";
import type { MemberImportResponse, MemberResponse, RoleRequestResponse } from "@/interfaces/member.interface";
import type { BaseResponse, Pagination, SingleResponse } from "@/utils/response-data";
import type { ErrorResponse } from "@/utils/error-response";

interface MemberState {
  members: MemberResponse[];
  roleRequests: RoleRequestResponse[];
  selectedMember: MemberResponse | null;

  loading: boolean;
  error: string | null;

  actionLoading: boolean;
  actionMessage: string | null;

  pagination: Pagination | null;
}

const initialState: MemberState = {
  members: [],
  roleRequests: [],
  selectedMember: null,

  loading: false,
  error: null,

  actionLoading: false,
  actionMessage: null,

  pagination: null,
};

const memberSlice = createSlice({
  name: "members",
  initialState,
  reducers: {
    clearMemberError(state) {
      state.error = null;
    },
    clearActionMessage(state) {
      state.actionMessage = null;
    },
    clearSelectedMember(state) {
      state.selectedMember = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(getPendingAuthorRequestsThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getPendingAuthorRequestsThunk.fulfilled, (state, action) => {
        const payload = action.payload as BaseResponse<RoleRequestResponse>;
        state.loading = false;
        state.roleRequests = payload.data;
      })
      .addCase(getPendingAuthorRequestsThunk.rejected, (state, action) => {
        state.loading = false;
        const err = action.payload as ErrorResponse;
        state.error = err?.status ?? "Không lấy được danh sách yêu cầu nâng cấp quyền";
      });

    builder
      .addCase(getMembersThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getMembersThunk.fulfilled, (state, action) => {
        const payload = action.payload as BaseResponse<MemberResponse>;
        state.loading = false;
        state.members = payload.data;
        state.pagination = payload.pagination ?? null;
      })
      .addCase(getMembersThunk.rejected, (state, action) => {
        state.loading = false;
        const err = action.payload as ErrorResponse;
        state.error = err?.status ?? "Không lấy được danh sách thành viên";
      });

    builder
      .addCase(approveMemberThunk.pending, (state) => {
        state.actionLoading = true;
        state.error = null;
        state.actionMessage = null;
      })
      .addCase(approveMemberThunk.fulfilled, (state, action) => {
        const payload = action.payload as SingleResponse<string>;
        state.actionLoading = false;
        state.actionMessage = payload.message;
      })
      .addCase(approveMemberThunk.rejected, (state, action) => {
        state.actionLoading = false;
        const err = action.payload as ErrorResponse;
        state.error = err?.status ?? "Duyệt thành viên thất bại";
      });

    builder
      .addCase(rejectMemberThunk.pending, (state) => {
        state.actionLoading = true;
        state.error = null;
        state.actionMessage = null;
      })
      .addCase(rejectMemberThunk.fulfilled, (state, action) => {
        const payload = action.payload as SingleResponse<string>;
        state.actionLoading = false;
        state.actionMessage = payload.message;
      })
      .addCase(rejectMemberThunk.rejected, (state, action) => {
        state.actionLoading = false;
        const err = action.payload as ErrorResponse;
        state.error = err?.status ?? "Từ chối thành viên thất bại";
      });

    builder
      .addCase(updateMemberStatusThunk.pending, (state) => {
        state.actionLoading = true;
        state.error = null;
      })
      .addCase(updateMemberStatusThunk.fulfilled, (state, action) => {
        const payload = action.payload as SingleResponse<MemberResponse>;
        state.actionLoading = false;
        state.actionMessage = payload.message;
        state.members = state.members.map((m) =>
          m.userId === payload.data.userId ? payload.data : m
        );
      })
      .addCase(updateMemberStatusThunk.rejected, (state, action) => {
        state.actionLoading = false;
        const err = action.payload as ErrorResponse;
        state.error = err?.status ?? "Cập nhật trạng thái thất bại";
      });

    builder
      .addCase(getMemberDetailThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(getMemberDetailThunk.fulfilled, (state, action) => {
        const payload = action.payload as SingleResponse<MemberResponse>;
        state.loading = false;
        state.selectedMember = payload.data;
      })
      .addCase(getMemberDetailThunk.rejected, (state, action) => {
        state.loading = false;
        const err = action.payload as ErrorResponse;
        state.error = err?.status ?? "Không lấy được chi tiết thành viên";
      });

    builder
      .addCase(importMembersThunk.pending, (state) => {
        state.actionLoading = true;
        state.error = null;
      })
      .addCase(importMembersThunk.fulfilled, (state, action) => {
        const payload = action.payload as SingleResponse<MemberImportResponse>;
        state.actionLoading = false;
        state.actionMessage = payload.message;
      })
      .addCase(importMembersThunk.rejected, (state, action) => {
        state.actionLoading = false;
        const err = action.payload as ErrorResponse;
        state.error = err?.status ?? "Import thành viên thất bại";
      });
  },
});

export const memberActions = memberSlice.actions;
export default memberSlice.reducer;
