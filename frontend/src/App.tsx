import { createBrowserRouter, RouterProvider } from "react-router-dom";

import RootLayout from "./components/RootLayout";
import HomeNavbar from "./components/HomeNavBar";
import HomeContent from "./components/HomeContent";
import LogInContent from "./components/LogInContent";
import SignUpContent from "./components/SignUpContent";
import MarketDetail from "./components/MarketDetail/MarketDetail";
import AccountOverview from "./components/AccountOverview";
import AdminPanel from "./components/AdminPanel/AdminPanel";

import { getAuthToken } from "./util/Auth";
import { logInAction, signUpAction, logOutAction } from "./util/Actions.ts";
import {
  marketProposalsLoader,
  marketLoader,
  accountLoader,
  marketsReadyForResolutionLoader,
  homeLoader,
} from "./util/Loaders.ts";

import "./style/App.css";
import ResolutionPanel from "./components/ResolutionPanel/ResolutionPanel.tsx";
import ErrorElement from "./components/ErrorElement.tsx";
import About from "./components/About.tsx";

const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    loader: getAuthToken,
    id: "root",
    errorElement: <ErrorElement />,
    children: [
      {
        path: "/",
        element: <HomeNavbar />,
        children: [
          {
            path: "/",
            id: "home",
            element: <HomeContent />,
            loader: homeLoader,
          },
          {
            path: "/about",
            element: <About />,
          },
          {
            path: "/market/:marketId",
            element: <MarketDetail />,
            loader: marketLoader,
          },
          {
            path: "/account",
            element: <AccountOverview />,
            loader: accountLoader,
          },
          {
            path: "/admin",
            element: <AdminPanel />,
            loader: marketProposalsLoader,
          },
          {
            path: "/resolve",
            element: <ResolutionPanel />,
            loader: marketsReadyForResolutionLoader,
          },
          {
            path: "/error",
            element: <ErrorElement />,
          },
          {
            path: "/auth",
            children: [
              { path: "login", element: <LogInContent />, action: logInAction },
              {
                path: "signup",
                element: <SignUpContent />,
                action: signUpAction,
              },
              { path: "logout", loader: logOutAction },
            ],
          },
        ],
      },
    ],
  },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
