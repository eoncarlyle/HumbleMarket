import { decodeJwt } from "jose";

type ValidationField = { valid: boolean; message: string}

export type ValidationData = {
  email: ValidationField; password: ValidationField; passwordConf: ValidationField;
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

export function getBaseUrl() {
  const hostname = new URL(window.location.href).hostname;
  return "http://" + hostname + ":8080"
}

export async function getAuthenticatedResponse(requestSubpath: string, method: string) {
  return fetch(getBaseUrl() + requestSubpath, {
    method: method,
    headers: {
      Authorization: String(getAuthToken()),
    },
  });
}