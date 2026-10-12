export type Role = 'USER' | 'ADMIN'; 

export interface User {
  id: string;
  email: string;
  role: Role;
  consentGivenAt: string; 
  createdAt: string;
  updatedAt: string;
}

export interface UserCreateRequest {
  email: string;
  password: string;
  consent: boolean;
}

export interface UserUpdateRequest {
  currentPassword: string;
  email?: string;
  newPassword?: string;
}
