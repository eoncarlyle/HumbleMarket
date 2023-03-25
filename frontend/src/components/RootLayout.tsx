import { useEffect } from "react";
import { Outlet, useLoaderData, useSubmit } from "react-router-dom";
import { tokenDuration, tokenExpired } from "../util/Auth";

function RootLayout() {
  const token = useLoaderData();
  const submit = useSubmit();

  //TODO: This does not work in cases where the user logs back in after their token is expired due to a 401 error.
  //TODO: Automatic logout needs to be carried out, proper error handling needs to take place, or both
  useEffect(() => {
    if (!token) {
      return;
    } else if (tokenExpired()) {
      submit(null, { action: "/auth/logout", method: "post" });
      return;
    }

    setTimeout(() => {
      submit(null, { action: "/auth/logout", method: "post" });
    }, tokenDuration());
  }, [token, submit]);

  return <Outlet />;
}

export default RootLayout;
