import { createBrowserRouter, RouterProvider } from "react-router-dom";

import RootLayout from "./components/RootLayout";
import HomeNavbar from "./components/HomeNavBar";
import AdminPanel from "./components/AdminPanel/AdminPanel";

import { loader as marketProposalsLoader } from "./util/MarketProposalsLoader.ts"

import "./style/App.css"

const router = createBrowserRouter([{
  path: "/",
  element: <RootLayout />,
  id: "root",
  children: [
    {
      path: "/",
      element: <HomeNavbar />,
      children: [
        { path: "/admin", element: <AdminPanel />, loader: marketProposalsLoader },
      ]
    }
  ]
},
]);

function App() {
  return <RouterProvider router={router} />;
}

export default App;