import { getAuthToken } from "./Auth";

export async function loader() {
  if (getAuthToken()) {
    //TODO: may want to centralise this
    const hostname = new URL(window.location.href).hostname;
    //TODO: String wrapping isn't great, as `getAuthToken` might be none, living with this for now
    const response = await fetch("http://" + hostname + ":8080/market", {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: String(getAuthToken()),
      },
    });
    //TODO: Flesh this out better

    const responseData = await response.json();
    return responseData;
  }
  return null;
}
