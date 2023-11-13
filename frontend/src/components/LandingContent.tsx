import { Button } from 'react-bootstrap'
import { LinkContainer } from 'react-router-bootstrap'
import '../style/LandingContent.module.css'

export default function LandingContent() {
  return (
    <>
      <h1>Think you have what it takes?</h1>
      <p>Create your own markets, compete against your friends and the public</p>
      <LinkContainer to="/auth/signup"><Button variant='success'>Sign Up</Button></LinkContainer>
      <LinkContainer to="/auth/login"><Button variant='primary'>Log In</Button></LinkContainer>
    </>
  )
}
