import { redirect } from "react-router-dom";
import sha256 from "crypto-js/sha256";
import * as EmailValidator from "email-validator";

import { setAuthToken, getBaseUrl } from "./Auth";
import AuthValidationData from "./AuthValidationData";

//TODO: trim email input on this and SignUp equivalent

export async function action({ request }: { request: Request }) {
  const formData = await request.formData();

  const validationData: AuthValidationData = {
    email: { valid: true, message: "" },
    password: { valid: true, message: "" },
    passwordConf: { valid: true, message: "" },
  };
  const email = formData.get("email") as string;
  const password = formData.get("password") as string;

  //Pre-request validation
  if (!EmailValidator.validate(email)) {
    validationData.email = { valid: false, message: "Please provide a valid email" };
  }

  if (password.length === 0) {
    validationData.password = { valid: false, message: "Please provide a password" };
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

  const response = await fetch(getBaseUrl() + "/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(authData),
  });

  if (!response.ok) {
    if (response.status === 403) {
      validationData.email = { valid: false, message: "" };
      validationData.password = { valid: false, message: "Incorrect email or password" };
    } else {
      validationData.email = { valid: false, message: "Server error during authentication - please try again in a few minutes!" };
    }
    return validationData;
  }

  const responseData = await response.json();
  setAuthToken(responseData.token);

  return redirect("/");
}
