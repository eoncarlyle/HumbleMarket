import { getAuthToken, getAuthenticatedResponse } from "./Auth";

// TODO: Fix the typing on this
export async function loader({ params }: any) {
  if (getAuthToken()) {
    const response = await getAuthenticatedResponse("/market/" + params.marketId, "GET")

    //TODO: Flesh out the non-happy path better
    const responseData = await response.json();
    return responseData;
  }
  return null;
}
