import type { SingleResponse } from "../utils/response-data";
import { axiosInstance } from "../utils/axios-instance";
import { handleAxiosError } from "./error.apis";

export interface CheckPermissionRequest {
  permissionCode: string;
  resource?: string;
}

export interface PermissionCheckResponse {
  hasPermission: boolean;
  permissionCode: string;
  message: string;
  userRoles: string[];
  userPermissions: string[];
}

export interface CheckRoleRequest {
  roleCode: string;
}

export const checkPermission = async (
  request: CheckPermissionRequest
): Promise<SingleResponse<PermissionCheckResponse>> => {
  try {
    const res = await axiosInstance.post("/permissions/check", request);

    if (res.status !== 200) {
      throw handleAxiosError(res);
    }

    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const checkRole = async (
  roleCode: string
): Promise<SingleResponse<PermissionCheckResponse>> => {
  try {
    const res = await axiosInstance.get(`/permissions/check-role/${roleCode}`);

    if (res.status !== 200) {
      throw handleAxiosError(res);
    }

    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const checkAnyRole = async (
  roleCodes: string[]
): Promise<SingleResponse<PermissionCheckResponse>> => {
  try {
    const res = await axiosInstance.post("/permissions/check-any-role", roleCodes);

    if (res.status !== 200) {
      throw handleAxiosError(res);
    }

    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

