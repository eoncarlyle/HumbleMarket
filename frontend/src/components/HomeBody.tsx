import classes from "../styles/HomeSection.module.css";
import { Link, useRouteLoaderData } from "react-router-dom";

function HomeBody() {
  const token = useRouteLoaderData("root") as string;
  console.log(token);
  return (
    <body>
      {token ? (
        <></>
      ) : (
        <>
          <div className={classes.body}>
            <div className={classes.masthead}>Think you have what it takes?</div>
            <div className={classes.subtitle}>Create your own markets, compete against your friends or the public</div>
          </div>
          <div className={classes.auth}>
            <Link to="/auth/login" className={classes.bodyLogin}>
              Log in
            </Link>
            <Link to="/auth/signup" className={classes.bodySingup}>
              Sign up
            </Link>
          </div>
        </>
      )}
    </body>
  );
}

export default HomeBody;
