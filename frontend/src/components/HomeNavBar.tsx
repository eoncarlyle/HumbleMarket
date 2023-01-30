import classes from "./HomeNavBar.module.css";
import { Link } from "react-router-dom";

function HomeNavBar() {
  return (
    <>
      <div className={classes.navbar}>
        <div className={classes.logo}>Schmitt's Humblemarket</div>

        <div className={classes.navbarRight}>
          <Link to="/auth/login" className={classes.navLogin}>
            Log in
          </Link>
          <Link to="/auth/signup" className={classes.navSignup}>
            Sign up
          </Link>
        </div>
      </div>
    </>
  );
}

export default HomeNavBar;
