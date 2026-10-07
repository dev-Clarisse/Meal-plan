// Correspond au record Java UserResponse
export type Role = 'USER' | 'ADMIN'; // à ajuster selon ton enum Role

export interface User {
  id: string;
  email: string;
  role: Role;
  consentGivenAt: string; // Instant ISO-8601
  createdAt: string;
  updatedAt: string;
}

// Correspond au record Java UserCreateRequest
export interface UserCreateRequest {
  email: string;
  password: string;
  consent: boolean;
}

// UserUpdateRequest : je n'ai pas ce DTO, à compléter
export type UserUpdateRequest = Record<string, unknown>;
