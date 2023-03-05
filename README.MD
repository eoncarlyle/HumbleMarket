  **Project Goal**
- Create a (fake money) prediction market that allows for public and private prediction markets

**Pre-private launch To Do**
- Backend
  - Change prices to `BigDecimal` and credits to `double`, check that this works in sending to frontend without type changing
  - Finish stubbed tests
  - Time-dependant market logic
  - Minimal logging
  - Move JWT, other sensitive security aspects to application.properties 

- Frontend
  - Watch Kevin Powell's tutorials
    - [Flexbox](https://www.youtube.com/watch?v=hwbqquXww-U&list=PL4-IK0AVhVjMSb9c06AjRlTpvxL3otpUd&ab_channel=KevinPowell)
    - [Responsive grid](https://www.youtube.com/watch?v=sKFW3wek21Q&ab_channel=KevinPowell)
  - Unit testing
  - Individual market pages for transactions
  - Notification handling
    - Creator to close market
    - Resolution of market
  - Creating market pages

__Post-launch To Do__
- Implement frontend unit testing 
- Frontend: implement password-forgot-emails
- Show market preview for

- Question: How will future events be handled in a way that is persistent?
    - Storage is handled by the Markets table
    - Every 30 minutes, search the table and determine if a market is going to expire in the next 30 minutes or if it has already expired (think: app crashing right before market resolves, want to handle this accordingly)
        - There are many ways to think about this but every single one depends on a time-invariant resolution method making changes to the database
        
The Future
- Prediction contests
- USDC for real money _if and only if_ it is legal