import type { LoginRequest, LoginResponse } from "../interfaces/auth.interface";
import { axiosInstance } from "../utils/axios-instance";
import type { SingleResponse } from "../utils/response-data";
import { handleAxiosError } from "./error.apis";

export const loginAdmin = async (
  request: LoginRequest
): Promise<SingleResponse<LoginResponse>> => {
  const res = await axiosInstance.post("/auth/login", request);
  return res.data;
};

export const logoutAdmin = async (): Promise<SingleResponse<null>> => {
  const refreshToken =
    localStorage.getItem("refreshToken") ||
    sessionStorage.getItem("refreshToken");
  const res = await axiosInstance.post("/auth/logout", { refreshToken });
  return res.data;
};
