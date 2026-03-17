import type { AccountResponse } from "./account.interface";

export interface LoginRequest {
  email: string;
  password: string;
  keepLogin: boolean;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  account: AccountResponse;
}
