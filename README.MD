  **Project Goal**
- Create a (fake money) prediction market that allows for public and private prediction markets

**Pre-private launch To Do**
- Backend
  - Finish stubbed tests
  - Time-dependant market logic
  - Minimal logging
  - Move JWT, other sensitive security aspects to application.properties 

- Frontend
  - Watch Kevin Powell's tutorials
    - [Flexbox](https://www.youtube.com/watch?v=hwbqquXww-U&list=PL4-IK0AVhVjMSb9c06AjRlTpvxL3otpUd&ab_channel=KevinPowell)
    - [Responsive grid](https://www.youtube.com/watch?v=sKFW3wek21Q&ab_channel=KevinPowell)
  - Individual market pages for transactions
  - Notification handling
    - Creator to close market
    - Resolution of market
  - Creating market pages
    - Mutualy exclusive outcomes, limitations on claim lengths
  - Remove every implicit any
  - Remove mentions of 'body' in elements
  - Make idomatic CSS classes
  - Put external imports above internal imports
  - Review of var and const
  - Write `{MarketBox(market)` as `<MarketBox market={market} />` and `function MarketBox({ market }: { market: Market })`

__Post-launch To Do__
- Implement frontend unit testing 
- Frontend: implement password-forgot-emails
- Show market preview for

- Question: How will future events be handled in a way that is persistent?
    - Storage is handled by the Markets table
    - Every 30 minutes, search the table and determine if a market is going to expire in the next 30 minutes or if it has already expired (think: app crashing right before market resolves, want to handle this accordingly)
        - There are many ways to think about this but every single one depends on a time-invariant resolution method making changes to the database


__Frontend Notes__
- During login write the following information to local storage
  - User email
  - User credits
  - User notifications
- Don't let the user place an impossible transaction
  - Buy: Insufficient credits to buy with or shares to buy out
  - Sale: Insufficient shares to sell

**Backend Notes**
- We are using integers in many places that might be inappropriate if we were dealing with 10 orders of magnitude higher

The Future
- Prediction contests
- USDC for real money _if and only if_ it is legal


Helmet_Stability4_Visible
