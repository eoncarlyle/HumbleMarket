import classes from "../styles/Auth.module.css";
import { Form, Link, useActionData } from "react-router-dom";
import { type ValidationData } from "../util/Auth";

function SignUpBody() {
  const validationData = useActionData() as ValidationData;
  return (
    <>
      <div className={classes.sectionBox}>
        <div className={classes.sectionHeader}>Sign up</div>

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
              validationData && (validationData.errors.has("password") || validationData.errors.has("passwordConf"))
                ? classes.authInputError
                : classes.authInput
            }
            placeholder="Password"
          ></input>
          <input
            name="passwordConf"
            type="password"
            className={
              validationData && validationData.errors.has("passwordConf") ? classes.authInputError : classes.authInput
            }
            placeholder="Confirm Password"
          ></input>
          {/* <input name="signUpCode" type="text" className={classes.signInInput} placeholder="Login Code"></input> */}
          {validationData && validationData.messages && (
            <ul className={classes.authErrorFeedback}>
              {validationData.messages.map((message: any) => (
                <li key={message}>{message}</li>
              ))}
            </ul>
          )}
          <button type="submit" className={classes.authMainButton}>
            Create Account
          </button>
          {/* TODO: implement the login code before putting into production! */}
        </Form>
        <div className={classes.authSubtitle}>Already have an account?</div>
        <Link to="../login" className={classes.authLink}>
          Log in
        </Link>
      </div>
    </>
  );
}

export default SignUpBody;
