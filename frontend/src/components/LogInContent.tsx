import { Link, Form as RRForm, useActionData } from "react-router-dom";
import { Button, Form } from "react-bootstrap";

import { AuthValidationData } from "../util/Auth";
import "../style/AuthForm.module.css";

export default function LogInContent() {
  const validationData = useActionData() as AuthValidationData;
  const emaiIsInvalid = validationData && !validationData.email.valid;
  const passwordIsInvalid = validationData && !validationData.password.valid;

  return (
    <>
      <h2>Log In</h2>
      <RRForm method="post">
        <Form.Group className="mb-3" controlId="formBasicEmail">
          <Form.Control name="email" type="email" placeholder="Email" isInvalid={emaiIsInvalid} />
          <Form.Control.Feedback type="invalid">{validationData?.email?.message}</Form.Control.Feedback>
        </Form.Group>
        <Form.Group className="mb-3" controlId="formBasicPassword">
          <Form.Control name="password" type="password" placeholder="Password" isInvalid={passwordIsInvalid} />
          <Form.Control.Feedback type="invalid">{validationData?.password?.message}</Form.Control.Feedback>
        </Form.Group>
        <Button type="submit">Log In</Button>
      </RRForm>
      <p>Don't have an account?</p>
      <Link to="/auth/signup">Sign Up</Link>
    </>
  );
}
