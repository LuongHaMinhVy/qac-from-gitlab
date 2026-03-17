import type { AccountResponse } from "../interfaces/account.interface";

export const isAdmin = (account: any): boolean => {
  if (!account) {
    console.log("isAdmin: account is null");
    return false;
  }

  if (!account.roles) {
    console.log("isAdmin: account.roles is null/undefined", account);
    return false;
  }

  if (!Array.isArray(account.roles)) {
    console.log("isAdmin: account.roles is not an array", account.roles);
    return false;
  }
  
  // Allow both Admin and Editor to access the admin panel
  const allowedRoles = ["ROLE_ADMIN", "ROLE_EDITOR"];
  const hasAllowedRole = account.roles.some((role: string) => allowedRoles.includes(role));
  
  return hasAllowedRole;
};

export const hasAnyRole = (
  account: AccountResponse | null,
  roles: string[]
): boolean => {
  if (!account || !account.roles) return false;
  return roles.some((role) => account.roles.includes(role));
};

export const hasAllRoles = (
  account: AccountResponse | null,
  roles: string[]
): boolean => {
  if (!account || !account.roles) return false;
  return roles.every((role) => account.roles.includes(role));
};

export const hasPermission = (
  account: AccountResponse | null,
  permission: string
): boolean => {
  if (!account || !account.permissions) return false;
  return account.permissions.includes(permission);
};

export const hasAnyPermission = (
  account: AccountResponse | null,
  permissions: string[]
): boolean => {
  if (!account || !account.permissions) return false;
  return permissions.some((permission) => account.permissions.includes(permission));
};

