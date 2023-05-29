import { useEffect } from "react";
import { Outlet, useLoaderData, useSubmit } from "react-router-dom";
import { tokenDuration } from "../util/Auth";

function RootLayout() {
  const token = useLoaderData();
  const submit = useSubmit();
  useEffect(() => {
    if (token) {
      setTimeout(() => {
        submit(null, { action: "/auth/logout", method: "post" });
      }, tokenDuration());
    }
  }, [token, submit]);
  //TODO: Create timeout to delete token on expiration

  return <Outlet />;
}

export default RootLayout;
