import { createAsyncThunk } from "@reduxjs/toolkit";
import { 
  getAllAdmins, 
  createAdmin, 
  updateAdmin, 
  deleteAdmin, 
  assignRoles,
} from "@/apis/admin.apis";
import type { AdminRequest, AssignRolesRequest } from "@/apis/admin.apis";
import type { AdminResponse } from "@/apis/admin.apis";
import { handleAxiosError } from "@/apis/error.apis";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";
import type { ErrorResponse } from "@/utils/error-response";

export const getAllAdminsThunk = createAsyncThunk<
  BaseResponse<AdminResponse>,
  any,
  { rejectValue: ErrorResponse }
>("adminManagement/getAll", async (params, { rejectWithValue }) => {
  try {
    return await getAllAdmins(params);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const createAdminThunk = createAsyncThunk<
  SingleResponse<AdminResponse>,
  AdminRequest,
  { rejectValue: ErrorResponse }
>("adminManagement/create", async (request, { rejectWithValue }) => {
  try {
    return await createAdmin(request);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const updateAdminThunk = createAsyncThunk<
  SingleResponse<AdminResponse>,
  { id: number; request: AdminRequest },
  { rejectValue: ErrorResponse }
>("adminManagement/update", async ({ id, request }, { rejectWithValue }) => {
  try {
    return await updateAdmin(id, request);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const deleteAdminThunk = createAsyncThunk<
  SingleResponse<string>,
  number,
  { rejectValue: ErrorResponse }
>("adminManagement/delete", async (id, { rejectWithValue }) => {
  try {
    return await deleteAdmin(id);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});

export const assignRolesThunk = createAsyncThunk<
  SingleResponse<AdminResponse>,
  { id: number; request: AssignRolesRequest },
  { rejectValue: ErrorResponse }
>("adminManagement/assignRoles", async ({ id, request }, { rejectWithValue }) => {
  try {
    return await assignRoles(id, request);
  } catch (error) {
    return rejectWithValue(handleAxiosError(error));
  }
});
