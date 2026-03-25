export interface User {
  sub: string;
  name?: string;
  userId?: number;
  enterpriseName?: string;
  enterpriseId?: number;
  enterpriseSlug?: string;
  primaryColor?: string;
  secondaryColor?: string;
  role: string;
  iat?: number;
  exp?: number;
  // Add other claims if needed
}

export interface AuthResponse {
  token: string;
}

export interface LoginPayload {
  email: string;
  password: string;
  requiredRole?: string;
}

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
  enterpriseName?: string;
}
