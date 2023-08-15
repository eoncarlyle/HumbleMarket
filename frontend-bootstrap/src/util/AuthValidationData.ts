import ValidationField from "../model/ValidationField"

interface AuthValidationData {
  email: ValidationField;
  password: ValidationField;
  passwordConf: ValidationField;
}

export default AuthValidationData