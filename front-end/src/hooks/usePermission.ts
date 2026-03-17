import { useState } from "react";
import { checkPermission, checkRole, checkAnyRole } from "../apis/permission.apis";
import type { PermissionCheckResponse } from "../apis/permission.apis";

export const usePermission = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const checkPermissionAsync = async (
    permissionCode: string,
    resource?: string
  ): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      const response = await checkPermission({ permissionCode, resource });
      return response.data.hasPermission;
    } catch (err: any) {
      setError(err?.message || "Failed to check permission");
      return false;
    } finally {
      setLoading(false);
    }
  };

  const checkRoleAsync = async (roleCode: string): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      const response = await checkRole(roleCode);
      return response.data.hasPermission;
    } catch (err: any) {
      setError(err?.message || "Failed to check role");
      return false;
    } finally {
      setLoading(false);
    }
  };

  const checkAnyRoleAsync = async (roleCodes: string[]): Promise<boolean> => {
    setLoading(true);
    setError(null);
    try {
      const response = await checkAnyRole(roleCodes);
      return response.data.hasPermission;
    } catch (err: any) {
      setError(err?.message || "Failed to check roles");
      return false;
    } finally {
      setLoading(false);
    }
  };

  return {
    checkPermission: checkPermissionAsync,
    checkRole: checkRoleAsync,
    checkAnyRole: checkAnyRoleAsync,
    loading,
    error,
  };
};

