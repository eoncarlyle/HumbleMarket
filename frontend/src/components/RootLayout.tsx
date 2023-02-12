import { useEffect } from "react";
import { Outlet, useLoaderData, useSubmit } from "react-router-dom";
import { tokenDuration, tokenExpired } from "../util/auth";

function RootLayout() {
  const token = useLoaderData();
  const submit = useSubmit();
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
