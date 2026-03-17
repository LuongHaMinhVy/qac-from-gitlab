import { createAsyncThunk } from "@reduxjs/toolkit";
import type { LoginRequest } from "../../interfaces/auth.interface";
import { loginAdmin, logoutAdmin } from "../../apis/auth.apis";
import { handleAxiosError } from "../../apis/error.apis";

export const loginThunk = createAsyncThunk(
  "auth/login",
  async (payload: LoginRequest, { rejectWithValue }) => {
    try {
      const data = await loginAdmin(payload);
      return data;
    } catch (error) {
      console.error("Login error:", error);
      return rejectWithValue(handleAxiosError(error));
    }
  }
);

export const logoutThunk = createAsyncThunk(
  "auth/logout",
  async (_, { rejectWithValue }) => {
    try {
      console.log(
        "logoutThunk: starting logout (will call API before clearing storage)"
      );
      const data = await logoutAdmin();
      console.log("logoutThunk: logoutAdmin resolved, clearing storage now");

      try {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("account");
        sessionStorage.removeItem("accessToken");
        sessionStorage.removeItem("refreshToken");
        sessionStorage.removeItem("account");
      } catch (e) {
        console.warn("logoutThunk: error clearing storage", e);
      }

      return data;
    } catch (error) {
      console.error("logoutThunk: error calling logoutAdmin", error);

      try {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("account");
        sessionStorage.removeItem("accessToken");
        sessionStorage.removeItem("refreshToken");
        sessionStorage.removeItem("account");
      } catch (e) {
        console.warn("logoutThunk: error clearing storage after failure", e);
      }

      return rejectWithValue(handleAxiosError(error));
    }
  }
);

function decodeJwt(token: string) {
  try {
    const [, payload] = token.split(".");
    return JSON.parse(atob(payload));
  } catch {
    return null;
  }
}

function isTokenExpired(token: string | null): boolean {
  if (!token) return true;
  const decoded = decodeJwt(token);
  if (!decoded?.exp) return true;
  return Date.now() >= decoded.exp * 1000;
}

export const restoreAuthThunk = createAsyncThunk(
  "auth/restore",
  async (_, { rejectWithValue }) => {
    try {
      const accessToken =
        localStorage.getItem("accessToken") ||
        sessionStorage.getItem("accessToken");
      const saved =
        localStorage.getItem("account") || sessionStorage.getItem("account");

      if (!saved || !accessToken) {
        return rejectWithValue("No saved session");
      }

      if (isTokenExpired(accessToken)) {
        const refreshToken =
          localStorage.getItem("refreshToken") ||
          sessionStorage.getItem("refreshToken");

        if (!refreshToken || isTokenExpired(refreshToken)) {
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          localStorage.removeItem("account");
          sessionStorage.removeItem("accessToken");
          sessionStorage.removeItem("refreshToken");
          sessionStorage.removeItem("account");
          return rejectWithValue("Tokens expired");
        }

        try {
          const { axiosInstance } = await import("../../utils/axios-instance");
          const response = await axiosInstance.post("/auth/refresh-token", {
            refreshToken,
          });
          const newAccessToken = response.data.data.accessToken;
          localStorage.setItem("accessToken", newAccessToken);
          sessionStorage.setItem("accessToken", newAccessToken);
        } catch (error) {
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          localStorage.removeItem("account");
          sessionStorage.removeItem("accessToken");
          sessionStorage.removeItem("refreshToken");
          sessionStorage.removeItem("account");
          return rejectWithValue("Token refresh failed");
        }
      }

      const account = JSON.parse(saved);
      return {
        data: account,
      } as any;
    } catch (error) {
      console.error("Restore auth error:", error);
      return rejectWithValue(handleAxiosError(error));
    }
  }
);
