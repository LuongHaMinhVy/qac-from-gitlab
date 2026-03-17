import type React from "react";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Checkbox } from "@/components/ui/checkbox";
import { useDispatch, useSelector } from "react-redux";
import { isAdmin } from "@/utils/permissions";
import { useToast } from "@/components/ui/use-toast";
import { useNavigate } from "react-router";
import type { AppDispatch, RootState } from "@/store";
import { loginThunk } from "@/store/thunks/auth.thunk";
import { authActions } from "@/store/slices/auth.slice";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faEye, faEyeSlash } from "@fortawesome/free-solid-svg-icons";

export default function LoginPage() {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();
  const { toast } = useToast();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [keepLogin, setKeepLogin] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showErrorModal, setShowErrorModal] = useState(false);

  const {
    loading,
    error: reduxError,
    account,
  } = useSelector((state: RootState) => state.auth);

  const validate = () => {
    const emailValue = email.trim().toLowerCase();
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailValue) return "Vui lòng nhập email.";
    if (!re.test(emailValue)) return "Email không hợp lệ.";
    if (!password) return "Vui lòng nhập mật khẩu.";
    if (password.length < 6) return "Mật khẩu phải có ít nhất 6 ký tự.";

    return null;
  };

  const storeTokens = (loginResponse: any) => {
    if (keepLogin) {
      localStorage.setItem("accessToken", loginResponse.accessToken);
      localStorage.setItem("refreshToken", loginResponse.refreshToken);
      localStorage.setItem("account", JSON.stringify(loginResponse));
      sessionStorage.removeItem("accessToken");
      sessionStorage.removeItem("refreshToken");
      sessionStorage.removeItem("account");
    } else {
      sessionStorage.setItem("accessToken", loginResponse.accessToken);
      sessionStorage.setItem("refreshToken", loginResponse.refreshToken);
      sessionStorage.setItem("account", JSON.stringify(loginResponse));
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("account");
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const v = validate();
    if (v) {
      setError(v);
      return;
    }

    setError(null);

    try {
      const result = await dispatch(
        loginThunk({ email, password, keepLogin })
      ).unwrap();

      const userAccount = result.data.account;
      storeTokens(result.data);

      if (isAdmin(userAccount)) {
        setShowSuccessModal(true);
        setTimeout(() => navigate("/admin/dashboard", { replace: true }), 2000);
      } else {
        toast({
          title: "Truy cập bị từ chối",
          description: "Tài khoản của bạn không có quyền truy cập trang quản trị.",
          variant: "destructive",
        });
        dispatch(authActions.logout());
      }
    } catch (err: any) {
      console.log(err);
      const msg = err?.message ?? "Đăng nhập thất bại";
      setError(msg);
      toast({
        title: "Lỗi đăng nhập",
        description: msg,
        variant: "destructive",
      });
    }
  };

  useEffect(() => {
    if (account && isAdmin(account.account)) {
      navigate("/admin/dashboard", { replace: true });
    }
  }, [account, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background p-4">
      <div className="w-full max-w-md">
        <div className="bg-card rounded-lg border border-border p-8 shadow-sm">
          <div className="mb-8 text-center">
            <h1 className="text-3xl font-bold text-foreground">Tạp chí QAC</h1>
            <p className="text-muted-foreground mt-2">
              Đăng nhập vào hệ thống quản trị
            </p>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            {(error || reduxError) && (
              <div className="text-sm text-red-700 bg-red-50 border border-red-100 p-3 rounded">
                {error || reduxError.message}
              </div>
            )}

            <div className="space-y-2">
              <label
                htmlFor="email"
                className="text-sm font-medium text-foreground"
              >
                Email
              </label>
              <Input
                id="email"
                type="email"
                placeholder="admin@qac.vn"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full"
              />
            </div>

            <div className="space-y-2 relative">
              <label
                htmlFor="password"
                className="flex items-center justify-between text-sm font-medium text-gray-700"
              >
                <span>Mật khẩu</span>
              </label>
              <div
                onClick={() => setShowPassword((s) => !s)}
                className="absolute cursor-pointer right-2 top-8.5"
              >
                {showPassword ? (
                  <FontAwesomeIcon icon={faEye} />
                ) : (
                  <FontAwesomeIcon icon={faEyeSlash} />
                )}
              </div>
              <Input
                id="password"
                name="password"
                type={showPassword ? "text" : "password"}
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full"
                autoComplete="current-password"
              />
            </div>

            <div className="flex items-center space-x-2">
              <Checkbox
                id="keep-login"
                checked={keepLogin}
                className="cursor-pointer"
                onCheckedChange={(checked) => setKeepLogin(checked as boolean)}
              />
              <label
                htmlFor="keep-login"
                className="text-sm font-medium text-foreground cursor-pointer"
              >
                Duy trì đăng nhập
              </label>
            </div>

            <Button disabled={loading} type="submit" className="w-full">
              {loading ? "Đang đăng nhập..." : "Đăng nhập"}
            </Button>
          </form>
        </div>

        <p className="text-center text-sm text-muted-foreground mt-6">
          © 2025 Tạp chí QAC. All rights reserved.
        </p>
      </div>
    </div>
  );
}
