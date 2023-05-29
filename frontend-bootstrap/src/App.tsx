import { createBrowserRouter, RouterProvider } from "react-router-dom";

import RootLayout from "./components/RootLayout";
import HomeNavbar from "./components/HomeNavBar";
import HomeContent from "./components/HomeContent";
import LogInContent from "./components/LogInContent";
import SignUpContent from "./components/SignUpContent";
import MarketDetail from "./components/MarketDetail/MarketDetail"
import AccountOverview from "./components/AccountOverview";

import { getAuthToken } from "./util/Auth";
import { action as logInAction } from "./util/LogInAction.ts";
import { action as signUpAction } from "./util/SignUpAction.ts";
import { action as logOutAction } from "./util/LogOutAction.ts"
import { loader as homeLoader } from "./util/MarketsLoader.ts"
import { loader as marketLoader} from "./util/MarketLoader.ts"
import { loader as accountLoader } from "./util/AccountLoader.ts"

import "./style/App.css"

const router = createBrowserRouter([{
  path: "/",
  element: <RootLayout />,
  loader: getAuthToken, 
  id: "root",
  children: [
    {
      path: "/",
      element: <HomeNavbar />,
      children: [
        { path: "/", id: "home", element: <HomeContent/>, loader: homeLoader },
        { path: "/market/:seqId", element: <MarketDetail/>, loader: marketLoader},
        { path: "/account", element: <AccountOverview/>, loader: accountLoader},
        {
          path: "/auth",
          children: [
            {path: "login", element: <LogInContent/>, action: logInAction},
            {path: "signup", element: <SignUpContent/>, action: signUpAction},
            {path: "logout", loader: logOutAction},
          ]
        }
      ]
    }
  ]
},
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;