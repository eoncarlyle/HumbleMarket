import { createBrowserRouter, RouterProvider } from "react-router-dom";
import AuthNavBar from "./components/AuthNavBar";
import LogInContent from "./components/LogInContent";
import SignUpBody from "./components/SignUpBody";
import SingleMarketBody from "./components/SingleMarketBody";
import { action as signUpAction } from "./util/SignUpAction";
import { action as logInAction } from "./util/LogInAction";
import { loader as homeLoader } from "./util/MarketsLoader";
import { loader as accountLoader } from "./util/AccountLoader";
import { loader as singleMarketLoader } from "./util/SingleMarketLoader";
import { getAuthToken } from "./util/Auth";
import { action as logOutAction } from "./util/LogoutAction";
import RootLayout from "./components/RootLayout";
import HomeNavBar from "./components/HomeNavBar";
import HomeContent from "./components/HomeContent";
import ErrorPlaceholder from "./components/ErrorPlaceholder";
import Account from "./components/Account";

const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    loader: getAuthToken,
    id: "root",
    children: [
      {
        path: "/",
        element: <HomeNavBar />,
        loader: homeLoader,
        id: "home",
        children: [
          { path: "/", element: <HomeContent /> },
          { path: "/market/:seqId", element: <SingleMarketBody />, loader: singleMarketLoader },
          { path: "/account", element: <Account />, loader: accountLoader },
        ],
      },
      {
        path: "auth",
        element: <AuthNavBar />,
        children: [
          { path: "login", element: <LogInContent />, action: logInAction },
          { path: "signup", element: <SignUpBody />, action: signUpAction },
          { path: "logout", action: logOutAction },
        ],
      },
    ],
    errorElement: <ErrorPlaceholder />,
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
