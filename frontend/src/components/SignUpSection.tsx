import classes from "./SignUp.module.css";
import { Link } from "react-router-dom";

function SignUpSection() {
  return (
    <>
      <div className={classes.sectionBox}>
        <div className={classes.sectionHeader}>Sign up</div>
        <input className={classes.signInInput} placeholder="Email"></input>
        <input className={classes.signInInput} placeholder="Password"></input>
        <input
          className={classes.signInInput}
          placeholder="Confirm Password"
        ></input>
        <input className={classes.signInInput} placeholder="Login Code"></input>
        <button className={classes.createAccount}>Create Account</button>
        <div className={classes.signInSubtitle}>Already have an account?</div>
        <Link to="../login" className={classes.signInLink}>
          Log in
        </Link>
      </div>
    </>
  );
}

export default SignUpSection;
