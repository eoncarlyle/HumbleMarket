import { getAuthToken } from "./Auth";

// TODO: Fix the typing on this
export async function loader({ params }: any) {
  if (getAuthToken()) {
    //TODO: may want to centralise this
    const hostname = new URL(window.location.href).hostname;
    //TODO: String wrapping isn't great, as `getAuthToken` might be none, living with this for now

    const response = await fetch("http://" + hostname + ":8080/user/data", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: String(getAuthToken()),
      },
    });
    //TODO: Flesh this out better

    //if (!response.ok) {
    //  return {
    //    message: "Backend failure!",
    //  };
    //}
    const responseData = await response.json();
    return responseData;
  }
  return null;
}
