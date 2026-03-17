import type {
  CategoryRequest,
  CategoryResponse,
  CategorySearchRequest,
} from "@/interfaces/category.interface";
import type { BaseResponse, SingleResponse } from "@/utils/response-data";
import { handleAxiosError } from "./error.apis";
import { axiosInstance, axiosInstanceMediaType } from "@/utils/axios-instance";

export const getAllCategories = async (
  searchRequest: CategorySearchRequest
): Promise<BaseResponse<CategoryResponse>> => {
  try {
    const res = await axiosInstance.get("/categories", {
      params: {
        search: searchRequest.search,
        page: searchRequest.page,
        size: searchRequest.size,
        sort: searchRequest.sort,
        direction: searchRequest.direction,
      },
    });
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    console.log(res.data, searchRequest);
    
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const createCategory = async (
  request: CategoryRequest,
  file?: File
): Promise<SingleResponse<CategoryResponse>> => {
  try {
    const formData = new FormData();
    const blob = new Blob([JSON.stringify(request)], { type: "application/json" });
    formData.append("request", blob);

    if (file) {
      formData.append("file", file);
    }

    const res = await axiosInstanceMediaType.post(`/categories`, formData);

    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};


export const updateCategory = async (
  id: number,
  request: CategoryRequest,
  file?: File
): Promise<SingleResponse<CategoryResponse>> => {
  try {
    const formData = new FormData();
    const blob = new Blob([JSON.stringify(request)], { type: "application/json" });
    formData.append("request", blob);

    if (file) {
      formData.append("file", file);
    }

    const res = await axiosInstanceMediaType.put(`/categories/${id}`, formData);

    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};


export const deleteCategory = async (
  id: number,
  deleteArticles: boolean
): Promise<SingleResponse<string>> => {
  try {
    const res = await axiosInstance.delete(`/categories/${id}`, {
      params: deleteArticles,
    });
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
