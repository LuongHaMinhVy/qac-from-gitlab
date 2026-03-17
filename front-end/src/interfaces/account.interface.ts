export interface AccountResponse {
  id: number;
  username?: string;
  fullName?: string;
  email: string;
  roles: string[];
  permissions: string[];
  status: boolean;
}
