import classes from "./NavBar.module.css";
import { Outlet } from "react-router-dom";
import { Link } from "react-router-dom";

function NavBar() {
  return (
    <>
      <div className={classes.navbar}>
        <Link to="/" className={classes.logoCentered}>
          Schmitt's Humblemarket
        </Link>
      </div>
      <Outlet />
    </>
  );
}

export default NavBar;
