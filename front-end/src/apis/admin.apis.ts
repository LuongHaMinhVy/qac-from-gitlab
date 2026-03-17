import { axiosInstance } from "@/utils/axios-instance";
import { handleAxiosError } from "./error.apis";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";

export interface AdminResponse {
  accountId: number;
  userId: number;
  username: string;
  email: string;
  fullName: string;
  roles: string[];
  permissions: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AdminRequest {
  username?: string;
  email?: string;
  fullName: string;
  password?: string;
  isActive: boolean;
  roleCodes?: string[];
}

export interface AssignRolesRequest {
  roleCodes: string[];
}

export const getAllAdmins = async (params: any): Promise<BaseResponse<AdminResponse>> => {
  try {
    const res = await axiosInstance.get("/admin/management", { params });
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createAdmin = async (request: AdminRequest): Promise<SingleResponse<AdminResponse>> => {
  try {
    const res = await axiosInstance.post("/admin/management", request);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateAdmin = async (id: number, request: AdminRequest): Promise<SingleResponse<AdminResponse>> => {
  try {
    const res = await axiosInstance.put(`/admin/management/${id}`, request);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteAdmin = async (id: number): Promise<SingleResponse<string>> => {
  try {
    const res = await axiosInstance.delete(`/admin/management/${id}`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const assignRoles = async (adminId: number, request: AssignRolesRequest): Promise<SingleResponse<AdminResponse>> => {
  try {
    const res = await axiosInstance.patch(`/admin/management/${adminId}/roles`, request);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
