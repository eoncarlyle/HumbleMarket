import * as EmailValidator from "email-validator";
import { setAuthToken, type ValidationData } from "../util/auth";
import { redirect } from "react-router-dom";

export async function action({ request }: any) {
  const formData = await request.formData();

  const validationData: ValidationData = { errors: new Set(), messages: [] };
  const email: string = formData.get("email");
  const password: string = formData.get("password");
  const passwordConf: string = formData.get("passwordConf");

  const passwordRule: RegExp = /^[a-zA-Z0-9!@#_\\-]*$/;

  //Pre-request validation
  if (!EmailValidator.validate(email)) {
    validationData.errors.add("email");
    validationData.messages.push("Valid email must be provided");
  }

  if (password.length < 8 || password.length > 64) {
    validationData.errors.add("password");
    validationData.messages.push("Passwords must be between 8 and 64 characters");
  }

  if (password.match(passwordRule)?.length !== 1) {
    validationData.errors.add("password");
    validationData.messages.push(
      "Passwords can only include letters, numbers, and the symbols '!', '@', '#', '_', and '-'"
    );
  }

  if (password !== passwordConf) {
    validationData.errors.add("passwordConf");
    validationData.messages.push("Passwords must match");
  }

  if (Array.from(validationData.errors.values()).length > 0) {
    return validationData;
  }

  //Request and post-request authentication
  const authData = {
    email: email,
    password: password,
  };

  const hostname = new URL(request.url).hostname;

  const response = await fetch("http://" + hostname + ":8080/auth/signup", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(authData),
  });

  if (!response.ok) {
    if (response.status === 401) {
      validationData.errors.add("email");
      validationData.messages.push("An account with this email has already been created");
    } else {
      validationData.messages.push("Server error during authentication - try again in a few minutes?");
    }
    return validationData;
  }

  const responseData = await response.json();
  setAuthToken(responseData.token);

  return redirect("/");
}
