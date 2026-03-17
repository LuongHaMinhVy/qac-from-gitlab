import { isAxiosError } from "axios";
import type { ErrorResponse } from "../utils/error-response";

export const handleAxiosError = (error: unknown): ErrorResponse => {
  if (isAxiosError(error)) {
    if (error.response) {
      const resData = error.response.data;
      const status = error.response.status;

      let message = "";
      if (typeof resData?.data === "string") {
        message = resData.data;
      } else {
        message = resData?.message || resData?.error;
      }
      
      if (!message) {
        if (status === 401) message = "Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.";
        else if (status === 403) message = "Bạn không có quyền thực hiện hành động này.";
        else if (status === 404) message = "Tài nguyên yêu cầu không tồn tại.";
        else if (status >= 500) message = "Hệ thống đang bảo trì. Vui lòng thử lại sau.";
        else message = "Đã xảy ra lỗi không xác định từ máy chủ.";
      }

      return {
        status: resData?.status || "ERROR",
        code: resData?.code || status || 500,
        message,
        data: resData?.errors ?? [],
      };
    }

    if (error.request) {
      return {
        status: "NETWORK_ERROR",
        code: 503,
        message: "Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng.",
      };
    }

    return {
      status: "AXIOS_ERROR",
      code: 500,
      message: "Lỗi kết nối dữ liệu: " + (error.message || "Không xác định"),
    };
  }

  return {
    status: "UNHANDLED_ERROR",
    code: 500,
    message: "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.",
  };
};
