import * as EmailValidator from "email-validator";
import { redirect } from "react-router-dom";
import sha256 from "crypto-js/sha256";

import { setAuthToken, getBaseUrl } from "./Auth";
import AuthValidationData from "./AuthValidationData";

export async function action({ request }: any) {
  const formData = await request.formData();

  const validationData: AuthValidationData = { email: { valid: true, message: "" }, password: { valid: true, message: "" }, passwordConf: { valid: true, message: "" }, };
  const email: string = formData.get("email");
  const password: string = formData.get("password");
  const passwordConf: string = formData.get("passwordConf");

  const passwordRule = /^[a-zA-Z0-9!@#_\\-]*$/;

  //Pre-request validation
  if (!EmailValidator.validate(email)) {
    validationData.email = { valid: false, message: "Please provide a valid email" };
  }

  if (password.length < 8 || password.length > 64) {
    validationData.password = { valid: false, message: "Passwords must be between 8 and 64 characters" }
  }

  if (password.match(passwordRule)?.length !== 1) {
    let formattedMessage: string;
    if (validationData.password.valid) {
      formattedMessage = "Passwords can only include letters, numbers, and the symbols '!', '@', '#', '_', and '-'";
    } else {
      formattedMessage = validationData.password.message + "\n, and can only include letters, numbers, and the symbols '!', '@', '#', '_', and '-'"
    }
    validationData.password = { valid: false, message: formattedMessage }
  }

  if (password !== passwordConf) {
    validationData.passwordConf = { valid: false, message: "Passwords must match" }
  }

  if (Object.values(validationData).filter((entry) => !entry.valid).length > 0) {
    return validationData;
  }

  let passwordHash = sha256(password).toString();

  //Request and post-request authentication
  const authData = {
    email: email,
    passwordHash: passwordHash,
  };

  const response = await fetch(getBaseUrl() + "/auth/signup", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(authData),
  });

  const responseData = await response.json();

  if (!response.ok) {
    if (response.status === 403) {
      const feedbackMessage = responseData?.message ? responseData.message : "An account with this email has already been created";
      validationData.email = { valid: false, message: feedbackMessage };
    } else {
      validationData.email = { valid: false, message: `HTTP error ${response.status} during authentication!` };
    }
    return validationData;
  }

  setAuthToken(responseData.token);
  return redirect("/");
}
