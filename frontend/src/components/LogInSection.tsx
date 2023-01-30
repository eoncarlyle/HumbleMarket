import classes from "./LogInSection.module.css";
import { Link } from "react-router-dom";

function LogInSection() {
  return (
    <>
      <div className={classes.sectionBox}>
        <div className={classes.sectionHeader}>Log in</div>
        <input className={classes.logInInput} placeholder="Email"></input>
        <input className={classes.logInInput} placeholder="Password"></input>
        <div className={classes.logInForgot}>
          <div className={classes.logInForgetPrompt}>Forgot password?</div>
          <div className={classes.logInForgetAction}>Reset It</div>
        </div>
        <button className={classes.logIn}>Log In</button>
        <div className={classes.signInSubtitle}>Don't have an account?</div>
        <Link to="../signup" className={classes.signUpLink}>
          Sign up
        </Link>
      </div>
    </>
  );
}

export default LogInSection;
