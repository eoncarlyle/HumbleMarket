import { useRouteError } from "react-router-dom";

//TODO: Make something more permanent

function ErrorPlaceholder() {
  var error: any = useRouteError();
  return (
    <>
      <div>Placeholder error</div>
      <div>{error}</div>
    </>
  );
}

export default ErrorPlaceholder;
