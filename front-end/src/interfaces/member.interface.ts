export interface MemberResponse {
  userId: number;
  accountId: number;
  username: string;
  email: string;
  fullName: string;
  phone: string;
  avatar: string;
  isActive: boolean;
  emailVerified: boolean;
  roles: string[];
  createdAt: string;
  updatedAt: string;
}

export interface SearchMemberRequest {
  email?: string;
  status?: string;
  role?: string;
  isActive?: boolean;
  page?: number;
  size?: number;
  sort?: string;
  direction?: "asc" | "desc";
}

export interface ApproveMemberRequest {
  roleRequestId: number;
  reviewComments: string;
}

export interface UserStatus {
  userId: number;
  isActive: boolean;
}

export interface MemberImportResponse {
  totalRows: number;
  successCount: number;
  failCount: number;
  message: string;
}

export interface MemberImportRow {
  username: string;
  email: string;
  roles: string;
  isActive?: boolean;
  emailVerified?: boolean;
  fullName: string;
  phone?: string;
  avatar?: string;
  bio?: string;
  dateOfBirth?: string;
  gender?: "MALE" | "FEMALE" | "OTHER";
  address?: string;
}

export interface RoleRequestResponse {
  id: number;
  accountId: number;
  accountEmail: string;
  accountUsername: string;
  requestedRoleCode: string;
  requestedRoleName: string;
  status: 'pending' | 'approved' | 'rejected';
  reason: string;
  reviewedById?: number;
  reviewComments?: string;
  reviewedAt?: string;
  createdAt: string;
}
