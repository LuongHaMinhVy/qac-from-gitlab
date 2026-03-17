import { useEffect, useState } from "react";
import { Route, Routes, Navigate, useNavigate } from "react-router";
import { useDispatch } from "react-redux";
import Login from "./layouts/Login";
import Admin from "./layouts/Admin";
import ProtectedRoute from "./components/ProtectedRoute";
import { restoreAuthThunk } from "./store/thunks/auth.thunk";
import type { AppDispatch } from "./store";
import Dashboard from "./layouts/Dashboard";
import Members from "./layouts/Members";
import Categories from "./layouts/Categories";
import Articles from "./layouts/Articles";
import Media from "./layouts/Media";
import RoleRequests from "./layouts/RoleRequests";
import AdminManagement from "./layouts/AdminManagement";
import BadWordManagement from "./layouts/BadWordManagement";
import ForceReloginModal from "./components/ForceLogoutModal";
import { Toaster } from "./components/ui/toaster";

function clearAllAuthStorage() {
  const keys = ["accessToken", "refreshToken", "account"];
  keys.forEach((k) => localStorage.removeItem(k));
  keys.forEach((k) => sessionStorage.removeItem(k));
}

export default function App() {
  const dispatch = useDispatch<AppDispatch>();
  const navigate = useNavigate();

  const [forceLogoutOpen, setForceLogoutOpen] = useState(false);
  const [forceLogoutMsg, setForceLogoutMsg] = useState(
    "Không thể kết nối tới máy chủ. Bạn sẽ được đưa về trang đăng nhập."
  );

  useEffect(() => {
    dispatch(restoreAuthThunk());
  }, [dispatch]);

  useEffect(() => {
    const onOffline = () => {
      setForceLogoutMsg(
        "Bạn đã mất kết nối Internet. Vui lòng kiểm tra mạng. Bạn sẽ được đưa về trang đăng nhập."
      );
      setForceLogoutOpen(true);
    };

    window.addEventListener("offline", onOffline);
    return () => window.removeEventListener("offline", onOffline);
  }, []);

  const handleForceLogoutConfirm = () => {
    setForceLogoutOpen(false);
    clearAllAuthStorage();
    navigate("/login", { replace: true });
  };

  return (
    <>
      <ForceReloginModal
        open={forceLogoutOpen}
        message={forceLogoutMsg}
        onConfirm={handleForceLogoutConfirm}
      />

      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/admin/*"
          element={
            <ProtectedRoute>
              <Admin />
            </ProtectedRoute>
          }
        >
          <Route index element={<Navigate to="dashboard" />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="articles" element={<Articles />} />
          <Route path="media" element={<Media />} />
          <Route path="members" element={<Members />} />
          <Route path="admins" element={<AdminManagement />} />
          <Route path="role-requests" element={<RoleRequests />} />
          <Route path="bad-words" element={<BadWordManagement />} />
          <Route path="categories" element={<Categories />} />
          <Route path="articles/*" element={<Articles />} />
        </Route>
        <Route path="/" element={<Navigate to="/admin" replace />} />
      </Routes>
      <Toaster />
    </>
  );
}
