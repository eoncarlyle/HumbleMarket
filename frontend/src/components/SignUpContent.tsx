import { Link, Form as RRForm, useActionData } from "react-router-dom";
import { Button, Form } from "react-bootstrap";

import AuthValidationData from "../util/AuthValidationData";
import "../style/AuthForm.module.css";

function SignUpContent() {
  const validationData = useActionData() as AuthValidationData;

  const emaiIsInvalid = validationData && !validationData.email.valid;
  const passwordIsInvalid = validationData && !validationData.password.valid;
  const passwordConfIsInvalid =
    validationData && !validationData.passwordConf.valid;

  return (
    <>
      <h2>Sign Up</h2>
      <RRForm method="post">
        <Form.Group className="mb-3" controlId="formBasicEmail">
          <Form.Control
            name="email"
            type="email"
            placeholder="Email"
            isInvalid={emaiIsInvalid}
          />
          {emaiIsInvalid ? (
            <Form.Control.Feedback type="invalid">
              {validationData.email.message}
            </Form.Control.Feedback>
          ) : (
            <></>
          )}
        </Form.Group>
        <Form.Group className="mb-3" controlId="formBasicPassword">
          <Form.Control
            name="password"
            type="password"
            placeholder="Password"
            isInvalid={passwordIsInvalid}
          />
          {passwordIsInvalid ? (
            <Form.Control.Feedback type="invalid">
              {validationData.password.message}
            </Form.Control.Feedback>
          ) : (
            <></>
          )}
        </Form.Group>
        <Form.Group className="mb-3" controlId="formBasicPassword">
          <Form.Control
            name="passwordConf"
            type="password"
            placeholder="Confirm Password"
            isInvalid={passwordConfIsInvalid}
          />
          {passwordConfIsInvalid ? (
            <Form.Control.Feedback type="invalid">
              {validationData.passwordConf.message}
            </Form.Control.Feedback>
          ) : (
            <></>
          )}
        </Form.Group>
        <Button type="submit">Sign Up</Button>
      </RRForm>
      <p>Already have an account?</p>
      <Link to="/auth/login">Log In</Link>
    </>
  );
}

export default SignUpContent;
