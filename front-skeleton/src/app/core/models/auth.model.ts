export interface LoginRequest {
  email: string;
  password: string;
}

// Correspond au record Java TokenResponse(accessToken, tokenType, expiresIn)
export interface TokenResponse {
  accessToken: string;
  tokenType: string;   // "Bearer"
  expiresIn: number;   // durée de vie en secondes
}
