import { createBrowserRouter, RouterProvider } from "react-router-dom";
import HomePage from "./pages/HomePage";
import NavBar from "./components/NavBar";
import LogInBody from "./components/LogInBody";
import SignUpBody from "./components/SignUpBody";
import { action as signUpAction } from "./util/SignUpAction";
import { action as logInAction } from "./util/LogInAction";
import { getAuthToken } from "./util/auth";
import { action as logOutAction } from "./components/Logout";
import RootLayout from "./components/RootLayout";

const router = createBrowserRouter([
  {
    path: "/",
    element: <RootLayout />,
    loader: getAuthToken,
    id: "root",
    children: [
      { path: "/", element: <HomePage /> },
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
