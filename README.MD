  **Project Goal**
- Create a (fake money) prediction market that allows for public and private prediction markets



**Pre-private Launch To Do**
- Description
  - Markets page, single market page, and account details tested on 4-5 users
  - No user-created markets, all resolution done by administrator

- Backend
  - mTLS
    - Followed this tutorial https://www.golinuxcloud.com/mutual-tls-authentication-mtls/#74_Create_server_certificate
    - Check out /var/db/mongodb/mongd.log: presenting cert while I need to actually present a public key, I think https://stackoverflow.com/questions/63195304/difference-between-pem-crt-key-files 
  - Time-dependant market logic
- Frontend
  - 

- Both
  - Transaction-like transactions

__Post-launch To Do__

- Frontend:
  - Enable notifications, market resolutions
  - Fix navbar taking up too much space on large screen sizes
  - Forms for market requests
  - Change forms to buttons

- Backend:
  - Finish stubbed tests
  - Figure out if merging marketId and marketSeqId is a good idea 
  - Logging aspects
  - Investigate transaction fees

- Both:
  - Password-forgot-emails  
  - Password-change-emails
  - Email to reset passwords

The Future
- Prediction contests
- USDC for real money _if and only if_ it is legal
