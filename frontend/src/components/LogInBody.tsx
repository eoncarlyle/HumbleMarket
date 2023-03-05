import classes from "../styles/Auth.module.css";
import { Form, Link, redirect, useActionData } from "react-router-dom";
import { type ValidationData } from "../util/auth";

function LogInBody() {
  const validationData = useActionData() as ValidationData;

  return (
    <>
      <div className={classes.sectionBox}>
        <div className={classes.sectionHeader}>Log in</div>

        <Form method="post">
          <input
            name="email"
            className={
              validationData && validationData.errors.has("email") ? classes.authInputError : classes.authInput
            }
            placeholder="Email"
          ></input>
          <input
            name="password"
            type="password"
            className={
              validationData && validationData.errors.has("password") ? classes.authInputError : classes.authInput
            }
            placeholder="Password"
          ></input>
          <div className={classes.authForgot}>
            {/* TODO: implement this email password reset logic */}
            {/* <div className={classes.logInForgetPrompt}>Forgot password?</div>
            <div className={classes.logInForgetAction}>Reset It</div> */}
          </div>
          {validationData && validationData.messages && (
            <ul className={classes.authErrorFeedback}>
              {validationData.messages.map((message: any) => (
                <li key={message}>{message}</li>
              ))}
            </ul>
          )}
          <button type="submit" className={classes.authMainButton}>
            Log In
          </button>
        </Form>
        <div className={classes.authSubtitle}>Don't have an account?</div>
        <Link to="../signup" className={classes.authLink}>
          Sign up
        </Link>
      </div>
    </>
  );
}

export default LogInBody;
