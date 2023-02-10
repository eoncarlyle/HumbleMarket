import classes from "../styles/LogInSection.module.css";
import signInClasses from "../styles/SignUp.module.css";
import { Form, Link, redirect, useActionData } from "react-router-dom";
import * as EmailValidator from "email-validator";

type ValidationData = {
  errors: Set<string>;
  messages: string[];
};

//TODO: Clean up CSS classes, a lot of shared classes between LogInSection and SignUpSection
//TODO: Hash passwords

function LogInSection() {
  const validationData = useActionData() as ValidationData;
  return (
    <>
      <div className={classes.sectionBox}>
        <div className={classes.sectionHeader}>Log in</div>

        <Form method="post">
          <input
            name="email"
            className={
              validationData && validationData.errors.has("email") ? signInClasses.signInInputError : classes.logInInput
            }
            placeholder="Email"
          ></input>
          <input
            name="password"
            type="password"
            className={
              validationData && validationData.errors.has("password")
                ? signInClasses.signInInputError
                : classes.logInInput
            }
            placeholder="Password"
          ></input>
          <div className={classes.logInForgot}>
            {/* TODO: implement this email password reset logic */}
            {/* <div className={classes.logInForgetPrompt}>Forgot password?</div>
            <div className={classes.logInForgetAction}>Reset It</div> */}
          </div>
          {validationData && validationData.messages && (
            <ul className={signInClasses.signInErrors}>
              {validationData.messages.map((message: any) => (
                <li key={message}>{message}</li>
              ))}
            </ul>
          )}
          <button type="submit" className={classes.logIn}>
            Log In
          </button>
        </Form>
        <div className={classes.signInSubtitle}>Don't have an account?</div>
        <Link to="../signup" className={classes.signUpLink}>
          Sign up
        </Link>
      </div>
    </>
  );
}

export default LogInSection;

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
  return redirect("/");
}
