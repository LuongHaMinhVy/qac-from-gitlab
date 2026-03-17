import { createAsyncThunk } from "@reduxjs/toolkit";
import { handleAxiosError } from "@/apis/error.apis";
import {
  approveMember,
  getMembers,
  rejectMember,
  updateMemberStatus,
  getMemberDetail,
  importMembersFromExcelFile,
  getPendingAuthorRequests,
} from "@/apis/members.apis";

import type {
  ApproveMemberRequest,
  SearchMemberRequest,
  UserStatus,
  MemberResponse,
  MemberImportResponse,
  RoleRequestResponse,
} from "@/interfaces/member.interface";

import type { BaseResponse, SingleResponse } from "@/utils/response-data";

export const getPendingAuthorRequestsThunk = createAsyncThunk<
  BaseResponse<RoleRequestResponse>,
  void,
  { rejectValue: unknown }
>("admin/members/author-requests/pending", async (_, { rejectWithValue }) => {
  try {
    return await getPendingAuthorRequests();
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const getMembersThunk = createAsyncThunk<
  BaseResponse<MemberResponse>,
  SearchMemberRequest,
  { rejectValue: unknown }
>("admin/members/search", async (payload, { rejectWithValue }) => {
  try {
    return await getMembers(payload);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const approveMemberThunk = createAsyncThunk<
  SingleResponse<string>,
  ApproveMemberRequest,
  { rejectValue: unknown }
>("admin/members/author-requests/approve", async (payload, { rejectWithValue }) => {
  try {
    return await approveMember(payload);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const rejectMemberThunk = createAsyncThunk<
  SingleResponse<string>,
  ApproveMemberRequest,
  { rejectValue: unknown }
>("admin/members/author-requests/reject", async (payload, { rejectWithValue }) => {
  try {
    return await rejectMember(payload);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const updateMemberStatusThunk = createAsyncThunk<
  SingleResponse<MemberResponse>,
  UserStatus,
  { rejectValue: unknown }
>("admin/members/update-status", async (payload, { rejectWithValue }) => {
  try {
    return await updateMemberStatus(payload);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const getMemberDetailThunk = createAsyncThunk<
  SingleResponse<MemberResponse>,
  number,
  { rejectValue: unknown }
>("admin/members/detail", async (userId, { rejectWithValue }) => {
  try {
    return await getMemberDetail(userId);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const importMembersThunk = createAsyncThunk<
  SingleResponse<MemberImportResponse>,
  File,
  { rejectValue: unknown }
>("admin/members/import", async (file, { rejectWithValue }) => {
  try {
    return await importMembersFromExcelFile(file);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});
