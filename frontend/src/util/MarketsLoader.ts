import { getAuthToken, getAuthenticatedResponse } from "./Auth";

export async function loader() {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/market", "GET")
    
    //TODO: Flesh out the non-happy path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}
