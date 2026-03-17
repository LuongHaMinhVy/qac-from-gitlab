import { axiosInstance, axiosInstanceMediaType } from "@/utils/axios-instance";
import type { MediaResponse } from "@/interfaces/media.interface";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";
import { handleAxiosError } from "./error.apis";

export const uploadMedia = async (file: File): Promise<SingleResponse<MediaResponse>> => {
  try {
    const formData = new FormData();
    formData.append("file", file);
    const res = await axiosInstanceMediaType.post("/media", formData);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getAllMedia = async (
  page: number = 0,
  size: number = 20,
  keyword?: string,
  mimeType?: string
): Promise<BaseResponse<MediaResponse>> => {
  try {
    const res = await axiosInstance.get("/media", {
      params: { page, size, keyword, mimeType, sortBy: "createdAt", direction: "desc" },
    });
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const softDeleteMedia = async (id: number): Promise<SingleResponse<string>> => {
  try {
    const res = await axiosInstance.delete(`/media/${id}`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const restoreMedia = async (id: number): Promise<SingleResponse<MediaResponse>> => {
  try {
    const res = await axiosInstance.post(`/media/${id}/restore`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const getMediaById = async (id: number): Promise<SingleResponse<MediaResponse>> => {
  try {
    const res = await axiosInstance.get(`/media/${id}`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
