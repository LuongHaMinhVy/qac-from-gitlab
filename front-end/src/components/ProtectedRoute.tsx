import { Navigate } from "react-router";
import { useSelector } from "react-redux";
import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import type { RootState, AppDispatch } from "../store";
import { isAdmin, hasPermission } from "../utils/permissions";
import { authActions } from "../store/slices/auth.slice";
import type { LoginResponse } from "../interfaces/auth.interface";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredPermission?: string;
}

export default function ProtectedRoute({ children, requiredPermission }: ProtectedRouteProps) {
  const dispatch = useDispatch<AppDispatch>();
  const { account } = useSelector((state: RootState) => state.auth);
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    const checkAndRestore = () => {
      if (account) {
        setIsChecking(false);
        return;
      }

      const accountStr = localStorage.getItem("account") || sessionStorage.getItem("account");
      const accessToken = localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken");
      
      if (accountStr && accessToken) {
        try {
          dispatch(authActions.restoreAccount());
          setIsChecking(false);
        } catch (error) {
          console.error("Error parsing account:", error);
          setIsChecking(false);
        }
      } else {
        setIsChecking(false);
      }
    };

    checkAndRestore();
  }, [account, dispatch]);
  if (isChecking) {
    return null;
  }
  const getAccount = (): LoginResponse | null => {
    if (account) return account;
    
    try {
      const accountStr = localStorage.getItem("account") || sessionStorage.getItem("account");
      const accessToken = localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken");
      
      if (accountStr && accessToken) {
        const parsed = JSON.parse(accountStr);
        if (parsed.account) {
          return parsed;
        } else if (parsed.roles) {
          return {
            accessToken: accessToken,
            refreshToken: localStorage.getItem("refreshToken") || sessionStorage.getItem("refreshToken") || "",
            account: parsed
          };
        }
      }
    } catch (error) {
      console.error("Error getting account:", error);
    }
    
    return null;
  };

  const currentAccount = getAccount();

  if (!currentAccount) {
    console.log("ProtectedRoute: No account, redirecting to login");
    return <Navigate to="/login" replace />;
  }
  if (!currentAccount.account) {
    console.log("ProtectedRoute: Invalid account structure, redirecting to login");
    return <Navigate to="/login" replace />;
  }
  
  if (requiredPermission) {
    if (!hasPermission(currentAccount.account, requiredPermission)) {
      console.log(`ProtectedRoute: Missing permission ${requiredPermission}, redirecting`);
      return <Navigate to="/unauthorized" replace />;
    }
  } else if (!isAdmin(currentAccount.account)) {
    console.log("ProtectedRoute: Not admin, redirecting to login");
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}

