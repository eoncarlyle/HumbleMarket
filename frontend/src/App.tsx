import { createBrowserRouter, RouterProvider } from "react-router-dom";
import HomePage from "./pages/Home";
import NavBar from "./components/NavBar";
import LogInSection from "./components/LogInSection";
import SignUpSection from "./components/SignUpSection";

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
          { path: "login", element: <LogInSection /> },
          { path: "signup", element: <SignUpSection /> },
        ],
      },
    ],
  },
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;
