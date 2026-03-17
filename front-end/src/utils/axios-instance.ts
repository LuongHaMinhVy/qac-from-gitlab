import { showNetworkModal } from "@/components/networkModal";
import { toast } from "@/components/ui/use-toast";
import axios from "axios";
import type {
  AxiosError,
  AxiosInstance,
  AxiosRequestConfig,
  InternalAxiosRequestConfig,
} from "axios";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1";

export const axiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json" },
  timeout: 10000,
});

export const axiosInstanceMediaType = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "multipart/form-data",
    Accept: "application/json",
  },
  timeout: 20000,
});

export const axiosInstanceFile = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "multipart/form-data" },
  timeout: 10000,
});

function getAccessToken(): string | null {
  return (
    localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken")
  );
}

function getRefreshToken(): string | null {
  return (
    localStorage.getItem("refreshToken") ||
    sessionStorage.getItem("refreshToken")
  );
}

function setAccessToken(newToken: string) {
  const useLocal = !!localStorage.getItem("refreshToken");
  (useLocal ? localStorage : sessionStorage).setItem("accessToken", newToken);
}

function clearAllAuthStorage() {
  const keysToRemove = ["accessToken", "refreshToken", "account"];
  keysToRemove.forEach((k) => localStorage.removeItem(k));
  keysToRemove.forEach((k) => sessionStorage.removeItem(k));
}

function decodeJwt(token: string) {
  try {
    const [, payload] = token.split(".");
    const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
    const padded = base64.padEnd(
      base64.length + ((4 - (base64.length % 4)) % 4),
      "=",
    );
    return JSON.parse(atob(padded));
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

function isNetworkOrServerDown(error: AxiosError): boolean {
  if (!error.response) return true;

  if (error.code === "ECONNABORTED") return true;

  return false;
}

function forceLogoutByNetwork(error: AxiosError) {
  showNetworkModal({
    message:
      "Không thể kết nối tới máy chủ. Vui lòng kiểm tra mạng hoặc thử lại sau. Bạn sẽ được đưa về trang đăng nhập.",
    onConfirm: () => {
      clearAllAuthStorage();
      window.location.href = "/login";
    },
  });

  return Promise.reject(error);
}

function attachAuthInterceptor(instance: AxiosInstance) {
  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const accessToken = getAccessToken();
      if (accessToken) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${accessToken}`;
      }
      return config;
    },
    (error) => Promise.reject(error),
  );
}

attachAuthInterceptor(axiosInstance);
attachAuthInterceptor(axiosInstanceMediaType);
attachAuthInterceptor(axiosInstanceFile);

let isRefreshing = false;
let refreshSubscribers: ((token: string | null) => void)[] = [];

function onRefreshed(token: string | null) {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
}

function addRefreshSubscriber(cb: (token: string | null) => void) {
  refreshSubscribers.push(cb);
}

const refreshClient = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: { "Content-Type": "application/json" },
});

type RetryRequestConfig = AxiosRequestConfig & { _retry?: boolean };

function attachRefreshInterceptor(instance: AxiosInstance) {
  instance.interceptors.response.use(
    (response) => response,
    async (err: unknown) => {
      const error = err as AxiosError;
      const originalRequest = (error.config || {}) as RetryRequestConfig;

      if (isNetworkOrServerDown(error)) {
        return forceLogoutByNetwork(error);
      }

      if (error.response?.status !== 401) {
        const status = error.response?.status;
        const responseData = error.response?.data as any;
        let errorMessage = responseData?.message || responseData?.error || null;
        if (typeof responseData?.data === "string") {
            errorMessage = responseData.data;
        }
        
        if (status === 400) {
          toast({
            title: "Yêu cầu không hợp lệ",
            description: errorMessage || "Dữ liệu gửi lên không đúng định dạng.",
            variant: "destructive",
          });
        } else if (status === 403) {
          toast({
            title: "Truy cập bị từ chối",
            description: errorMessage || "Bạn không có quyền thực hiện thao tác này.",
            variant: "destructive",
          });
        } else if (status === 404) {
          toast({
            title: "Không tìm thấy",
            description: errorMessage || "Dữ liệu bạn yêu cầu không tồn tại.",
            variant: "destructive",
          });
        } else if (status === 409) {
          toast({
            title: "Xung đột dữ liệu",
            description: errorMessage || "Dữ liệu đã tồn tại hoặc bị trùng lặp.",
            variant: "destructive",
          });
        } else if (status === 422) {
          toast({
            title: "Dữ liệu không hợp lệ",
            description: errorMessage || "Vui lòng kiểm tra lại thông tin đã nhập.",
            variant: "destructive",
          });
        } else if (status && status >= 500) {
          toast({
            title: "Lỗi máy chủ",
            description: errorMessage || "Máy chủ gặp sự cố. Vui lòng thử lại sau.",
            variant: "destructive",
          });
        }
        return Promise.reject(error);
      }

      const url = originalRequest?.url || "";
      if (url.includes("/auth/logout")) {
        return Promise.reject(error);
      }

      if (originalRequest._retry) return Promise.reject(error);
      originalRequest._retry = true;

      const refreshToken = getRefreshToken();
      if (!refreshToken || isTokenExpired(refreshToken)) {
        clearAllAuthStorage();
        window.location.href = "/login";
        return Promise.reject(error);
      }

      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          addRefreshSubscriber((newToken) => {
            if (!newToken) return reject(error);
            originalRequest.headers = originalRequest.headers || {};
            (originalRequest.headers as any)["Authorization"] =
              `Bearer ${newToken}`;
            resolve(instance(originalRequest));
          });
        });
      }

      isRefreshing = true;
      try {
        const res = await refreshClient.post("/auth/refresh-token", {
          refreshToken,
        });

        const newAccessToken = (res.data as any)?.data?.accessToken;
        if (!newAccessToken)
          throw new Error("No accessToken in refresh response");

        setAccessToken(newAccessToken);

        isRefreshing = false;
        onRefreshed(newAccessToken);

        originalRequest.headers = originalRequest.headers || {};
        (originalRequest.headers as any)["Authorization"] =
          `Bearer ${newAccessToken}`;

        return instance(originalRequest);
      } catch (refreshErr) {
        isRefreshing = false;
        onRefreshed(null);

        clearAllAuthStorage();
        window.location.href = "/login";
        return Promise.reject(refreshErr);
      }
    },
  );
}

attachRefreshInterceptor(axiosInstance);
attachRefreshInterceptor(axiosInstanceMediaType);
attachRefreshInterceptor(axiosInstanceFile);
