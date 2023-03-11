import classes from "../styles/HomeNavBar.module.css";
import { Link, Form, useRouteLoaderData, Outlet } from "react-router-dom";

function HomeNavBar() {
  const token = useRouteLoaderData("root") as string;
  return (
    <>
      <header className={classes.navbar}>
        <Link to="/" className={classes.logo}>
          Schmitt's Humblemarket
        </Link>
        <div className={classes.navbarRight}>
          {token ? (
            <Form action="/auth/logout" method="post">
              <button className={classes.navLogout}>Log out</button>
            </Form>
          ) : (
            <>
              <Link to="/auth/login" className={classes.navLogin}>
                Log in
              </Link>
              <Link to="/auth/signup" className={classes.navSignup}>
                Sign up
              </Link>
            </>
          )}
        </div>
      </header>
      <Outlet />
    </>
  );
}

export default HomeNavBar;
