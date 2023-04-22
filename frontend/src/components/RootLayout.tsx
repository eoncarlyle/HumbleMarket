import { useEffect } from "react";
import { Outlet, useLoaderData, useSubmit } from "react-router-dom";
import { tokenDuration, tokenExpired } from "../util/Auth";

function RootLayout() {
  const token = useLoaderData();
  const submit = useSubmit();

  //TODO: Create timeout to delete token on expiration 
  if (token && tokenExpired()) {
      submit(null, { action: "/auth/logout", method: "post" });
      return;
  }

  return <Outlet />;
}

export default RootLayout;
