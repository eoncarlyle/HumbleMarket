import { createBrowserRouter, RouterProvider } from "react-router-dom";
import NavBar from "./components/NavBar";
import LogInBody from "./components/LogInBody";
import SignUpBody from "./components/SignUpBody";
import SingleMarketBody from "./components/SingleMarketBody";
import { action as signUpAction } from "./util/SignUpAction";
import { action as logInAction } from "./util/LogInAction";
import { loader as homeLoader } from "./util/MarketsLoader";
import { loader as singleMarketLoader } from "./util/SingleMarketLoader";
import { getAuthToken } from "./util/Auth";
import { action as logOutAction } from "./components/Logout";
import RootLayout from "./components/RootLayout";
import HomeNavBar from "./components/HomeNavBar";
import HomeBody from "./components/HomeBody";

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
          { path: "/", element: <HomeBody /> },
          { path: "/market/:seqId", element: <SingleMarketBody />, loader: singleMarketLoader },
        ],
      },
      {
        path: "auth",
        element: <NavBar />,
        children: [
          { path: "login", element: <LogInBody />, action: logInAction },
          { path: "signup", element: <SignUpBody />, action: signUpAction },
          { path: "logout", action: logOutAction },
        ],
      },
    ],
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
