import type {
  ApproveMemberRequest,
  MemberImportResponse,
  MemberResponse,
  SearchMemberRequest,
  UserStatus,
} from "../interfaces/member.interface";
import { axiosInstance, axiosInstanceFile } from "../utils/axios-instance";
import type { BaseResponse, SingleResponse } from "../utils/response-data";
import { handleAxiosError } from "./error.apis";

export const getMembers = async (
  searchParam: SearchMemberRequest
): Promise<BaseResponse<MemberResponse>> => {
  try {
    const cleanParams = Object.fromEntries(
      Object.entries(searchParam).filter(([, v]) => v !== undefined && v !== "")
    );
    const res = await axiosInstance.get("/admin/members", {
      params: cleanParams,
    });
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const approveMember = async (
  approveRequest: ApproveMemberRequest
): Promise<SingleResponse<string>> => {
  try {
    const res = await axiosInstance.post(
      "/admin/members/author-requests/approve",
      approveRequest
    );
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const rejectMember = async (
  rejectRequest: ApproveMemberRequest
): Promise<SingleResponse<string>> => {
  try {
    const res = await axiosInstance.post(
      "/admin/members/author-requests/reject",
      rejectRequest
    );
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getPendingAuthorRequests = async (): Promise<
  BaseResponse<any>
> => {
  try {
    const res = await axiosInstance.get(
      "/admin/members/author-requests/pending"
    );
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateMemberStatus = async (
  request: UserStatus
): Promise<SingleResponse<MemberResponse>> => {
  try {
    const res = await axiosInstance.patch(
      `/admin/members/${request.userId}/status`,
      null,
      { params: { isActive: request.isActive } }
    );
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getMemberDetail = async (
  userId: number
): Promise<SingleResponse<MemberResponse>> => {
  try {
    const res = await axiosInstance.get(`/admin/members/${userId}`);
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const importMembersFromExcelFile = async (
  file: File
): Promise<SingleResponse<MemberImportResponse>> => {
  try {
    const formData = new FormData();
    formData.append("file", file);

    const res = await axiosInstanceFile.post("admin/members/import", formData);

    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
