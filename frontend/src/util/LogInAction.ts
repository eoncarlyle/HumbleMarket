import * as EmailValidator from "email-validator";
import { setAuthToken, type ValidationData } from "../util/auth";
import { redirect } from "react-router-dom";

export async function action({ request }: any) {
  const formData = await request.formData();

  const validationData: ValidationData = { errors: new Set(), messages: [] };
  const email: string = formData.get("email");
  const password: string = formData.get("password");

  //Pre-request validation
  if (!EmailValidator.validate(email)) {
    validationData.errors.add("email");
    validationData.messages.push("Please provide a valid email");
  }

  if (password.length === 0) {
    validationData.errors.add("password");
    validationData.messages.push("Please provide a password");
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

  const response = await fetch("http://" + hostname + ":8080/auth/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(authData),
  });

  if (!response.ok) {
    if (response.status === 401) {
      validationData.errors.add("email");
      validationData.errors.add("password");
      validationData.messages.push("Username or password is incorrect");
    } else {
      validationData.messages.push("Server error during authentication - try again in a few minutes?");
    }
    return validationData;
  }

  const responseData = await response.json();
  setAuthToken(responseData.token);

  return redirect("/");
}
