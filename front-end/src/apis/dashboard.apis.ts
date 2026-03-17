import type {
  DashboardChartResponse,
  DashboardSummaryResponse,
} from "@/interfaces/dashboard.interface";
import type { SingleResponse } from "@/utils/response-data";
import { handleAxiosError } from "./error.apis";
import { axiosInstance } from "@/utils/axios-instance";

export const fetchDashBoardSummary = async (): Promise<
  SingleResponse<DashboardSummaryResponse>
> => {
  try {
    const res = await axiosInstance.get(`/dashboard/summary`);
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};

export const fetchDashBoardCharts = async (): Promise<
  SingleResponse<DashboardChartResponse>
> => {
  try {
    const res = await axiosInstance.get(`/dashboard/charts`);
    if (res.status != 200) {
      throw handleAxiosError(res);
    }
    return res.data;
  } catch (error) {
    throw handleAxiosError(error);
  }
};
