import { Outlet } from "react-router-dom";
import { Link } from "react-router-dom";
import classes from "../styles/Auth.module.css";

function AuthNavBar() {
  return (
    <>
      <header className={classes.navbar}>
        <Link to="/" className={classes.logoCentered}>
          Schmitt's Humblemarket
        </Link>
      </header>
      <Outlet />
    </>
  );
}

export default AuthNavBar;
