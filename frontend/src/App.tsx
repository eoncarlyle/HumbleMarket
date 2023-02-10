import { createBrowserRouter, RouterProvider } from "react-router-dom";
import HomePage from "./pages/Home";
import NavBar from "./components/NavBar";
import LogInSection, { action as logInAction } from "./components/LogInSection";
import SignUpSection, { action as signUpAction } from "./components/SignUpSection";

//TODO: Clean up CSS: remove unused classes, make variables consistent and meaningful

const router = createBrowserRouter([
  {
    path: "/",
    children: [
      { path: "/", element: <HomePage /> },
      {
        path: "auth",
        element: <NavBar />,
        children: [
          { path: "login", element: <LogInSection />, action: logInAction },
          { path: "signup", element: <SignUpSection />, action: signUpAction },
        ],
      },
    ],
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
