import { decodeJwt } from "jose";

export type ValidationData = {
  errors: Set<string>;
  messages: string[];
};

export function setAuthToken(token: string): void {
  localStorage.setItem("token", token);
  localStorage.setItem("expire", String(decodeJwt(token).exp));
}

export function getAuthToken() {
  return localStorage.getItem("token");
}

export function getExpiration() {
  return localStorage.getItem("expire");
}

export function tokenExpired() {
  return tokenDuration() < 0;
}

export function tokenDuration() {
  return Number(getExpiration()) * 1000 - new Date().getTime();
}
