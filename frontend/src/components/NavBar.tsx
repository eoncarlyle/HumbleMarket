import classes from "../styles/Auth.module.css";
import { Outlet } from "react-router-dom";
import { Link } from "react-router-dom";

function NavBar() {
  return (
    <>
      <header className={classes.navbar}>
        <Link to="/" className={classes.logoCentered}>
          Schmitt's Humblemarket
        </Link>
      </header>
      <body>
        <Outlet />
      </body>
    </>
  );
}

export default NavBar;
