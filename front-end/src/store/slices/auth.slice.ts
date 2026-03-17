import { createSlice } from "@reduxjs/toolkit";
import {
  loginThunk,
  logoutThunk,
  restoreAuthThunk,
} from "../thunks/auth.thunk";
import type { LoginResponse } from "../../interfaces/auth.interface";

interface initialStateType {
  account: LoginResponse | null;
  loading: boolean;
  error: any;
}

const loadAccountFromStorage = (): LoginResponse | null => {
  try {
    const accountStr =
      localStorage.getItem("account") || sessionStorage.getItem("account");
    if (accountStr) {
      const parsed = JSON.parse(accountStr);
      if (parsed.account) {
        return parsed;
      }
      if (parsed.roles) {
        const accessToken =
          localStorage.getItem("accessToken") ||
          sessionStorage.getItem("accessToken") ||
          "";
        const refreshToken =
          localStorage.getItem("refreshToken") ||
          sessionStorage.getItem("refreshToken") ||
          "";
        return {
          accessToken: accessToken,
          refreshToken: refreshToken,
          account: parsed,
        };
      }
    }
  } catch (error) {
    console.error("Error loading account from storage:", error);
  }
  return null;
};

const initialState: initialStateType = {
  account: loadAccountFromStorage(),
  loading: false,
  error: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    logout: (state) => {
      state.account = null;
      state.loading = false;
      state.error = null;
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("account");
      sessionStorage.removeItem("accessToken");
      sessionStorage.removeItem("refreshToken");
      sessionStorage.removeItem("account");
    },
    restoreAccount: (state) => {
      const accountStr =
        localStorage.getItem("account") || sessionStorage.getItem("account");
      if (accountStr) {
        try {
          const parsed = JSON.parse(accountStr);
          if (parsed.account) {
            state.account = parsed;
          } else if (parsed.roles) {
            const accessToken =
              localStorage.getItem("accessToken") ||
              sessionStorage.getItem("accessToken") ||
              "";
            const refreshToken =
              localStorage.getItem("refreshToken") ||
              sessionStorage.getItem("refreshToken") ||
              "";
            state.account = {
              accessToken: accessToken,
              refreshToken: refreshToken,
              account: parsed,
            };
          } else {
            state.account = null;
          }
        } catch (error) {
          console.error("Error restoring account:", error);
          state.account = null;
        }
      }
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loginThunk.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.account = action.payload.data;
      })
      .addCase(loginThunk.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(logoutThunk.fulfilled, (state) => {
        state.account = null;
        state.loading = false;
        state.error = null;
      })
      .addCase(logoutThunk.rejected, (state, action) => {
        state.account = null; 
        state.loading = false;
        state.error = action.payload ?? action.error;
      })

      .addCase(restoreAuthThunk.pending, (state) => {
        state.loading = true;
      })
      .addCase(restoreAuthThunk.fulfilled, (state, action) => {
        state.loading = false;
        state.account = action.payload.data;
      })
      .addCase(restoreAuthThunk.rejected, (state) => {
        state.loading = false;
        state.account = null;
      });
  },
});

export default authSlice.reducer;
export const authActions = authSlice.actions;
