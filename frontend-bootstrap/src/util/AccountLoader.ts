import { getAuthenticatedResponse, getAuthToken } from "./Auth";

// TODO: Fix the typing on this
export async function loader() {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/user/data", "GET");
    
    //TODO: Flesh out non-happy-path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}
