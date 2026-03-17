import type {
  BadWordRequest,
  BadWordResponse,
  BadWordSearchRequest,
} from "@/interfaces/badword.interface";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";
import { handleAxiosError } from "./error.apis";
import { axiosInstance } from "@/utils/axios-instance";

export const getAllBadWords = async (
  searchRequest: BadWordSearchRequest
): Promise<BaseResponse<BadWordResponse>> => {
  try {
    const res = await axiosInstance.get("/bad-words", {
      params: {
        keyword: searchRequest.keyword,
        page: searchRequest.page || 0,
        size: searchRequest.size || 10,
        sort: searchRequest.sort || "createdAt",
        direction: searchRequest.direction || "desc",
      },
    });
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createBadWord = async (
  request: BadWordRequest
): Promise<SingleResponse<BadWordResponse>> => {
  try {
    const res = await axiosInstance.post("/bad-words", request);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const updateBadWord = async (
  id: number,
  request: BadWordRequest
): Promise<SingleResponse<BadWordResponse>> => {
  try {
    const res = await axiosInstance.put(`/bad-words/${id}`, request);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const deleteBadWord = async (
  id: number
): Promise<SingleResponse<string>> => {
  try {
    const res = await axiosInstance.delete(`/bad-words/${id}`);
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
