import { decodeJwt } from "jose";

//TODO Move this to model directory, change to interface:

const adminEmail = "admin@market.iainschmitt.com"


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

export function isAdmin() {
  return String(decodeJwt(getAuthToken()).email) === adminEmail;
}

export function getBaseUrl() {
  const hostname = new URL(window.location.href).hostname;
  //TODO: figure out why backend args aren't working
  //return "http://" + hostname + ":8090"
  return "http://" + hostname + ":8080"
}

export async function getAuthenticatedResponse(requestSubpath: string, method: string, body?: object) {
  if (body) {
    return fetch(getBaseUrl() + requestSubpath, {
      method: method,
      headers: {
        Authorization: String(getAuthToken()),
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });
  } else {
    try {
      return fetch(getBaseUrl() + requestSubpath, {
        method: method,
        headers: {
          Authorization: String(getAuthToken()),
        },
      });
    }
    catch (e: any) {
      throw e
    }
  }
}