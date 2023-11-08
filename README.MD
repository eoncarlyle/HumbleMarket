  **Project Goal**
- Create a (fake money) prediction market that allows for public and private prediction markets


**Pre-private Launch To Do**
- Admin panel
  - MarketProposalForm: use the id of the created market to then synthetically populate the list of markets to review
    - This will require a backend code change to return a JSON representation of the market proposal
  - MarketProposalReview
    - Change the modal to not _respond_ to the state but instead to be the element that sends the request
  - Unified state between MarketProposalReview and MarketProposalForm: might need to get creative

- Description
  - Markets page, single market page, and account details tested on 4-5 users
  - No user-created markets, all resolution done by administrator

- Backend
 - Integration testing 
- Frontend
  - Finish admin-only panel
  - Do browser size breakpoints to change inbetween the accoutns icon and something in text


__Post-launch To Do__

- Ops:
  - Java journal logging
  - Ansible playbooks for infrastructure
  - Scripts for creation of 

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
