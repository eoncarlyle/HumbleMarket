import classes from "../styles/SignUp.module.css";
import { Form, Link, redirect, useActionData } from "react-router-dom";
import * as EmailValidator from "email-validator";

type ValidationData = {
  errors: Set<string>;
  messages: string[];
};

//TODO: Hash passwords

function SignUpSection() {
  const validationData = useActionData() as ValidationData;
  return (
    <>
      <div className={classes.sectionBox}>
        <div className={classes.sectionHeader}>Sign up</div>

        <Form method="post">
          <input
            name="email"
            className={
              validationData && validationData.errors.has("email") ? classes.signInInputError : classes.signInInput
            }
            placeholder="Email"
          ></input>
          <input
            name="password"
            type="password"
            className={
              validationData && (validationData.errors.has("password") || validationData.errors.has("passwordConf"))
                ? classes.signInInputError
                : classes.signInInput
            }
            placeholder="Password"
          ></input>
          <input
            name="passwordConf"
            type="password"
            className={
              validationData && validationData.errors.has("passwordConf")
                ? classes.signInInputError
                : classes.signInInput
            }
            placeholder="Confirm Password"
          ></input>
          {/* <input name="signUpCode" type="text" className={classes.signInInput} placeholder="Login Code"></input> */}
          {validationData && validationData.messages && (
            <ul className={classes.signInErrors}>
              {validationData.messages.map((message: any) => (
                <li key={message}>{message}</li>
              ))}
            </ul>
          )}
          <button type="submit" className={classes.createAccount}>
            Create Account
          </button>
          {/* TODO: implement the login code before putting into production! */}
        </Form>
        <div className={classes.signInSubtitle}>Already have an account?</div>
        <Link to="../login" className={classes.signInLink}>
          Log in
        </Link>
      </div>
    </>
  );
}

export default SignUpSection;

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
  return redirect("/");
}
