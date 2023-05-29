  **Project Goal**
- Create a (fake money) prediction market that allows for public and private prediction markets



**Pre-private Launch To Do**
- Description
  - Markets page, single market page, and account details tested on 4-5 users
  - No user-created markets, all resolution done by administrator

- Backend
  - Implement transaction fees
  - Finish stubbed tests
  - Time-dependant market logic
  - MarketTransactionService changes in new branch
    - Sell-side changes
      - Per-share sell proceeds not to be calculated with $P = n_i (n_i + y_i)^{-1}$ but instead by $P=n_f(n_f + y_f)^{-1}$
      - This will require sending a table to the client of all $P$ for all possible values of $n_f$ and $y_f$ given client's current positions
      - This can be represented easily in JSON - given that each request for single-market information is already tied to a particular user account

    - Buy and sell side changes
      - Instead of trying to get overly creative with websockets for real-time pricing, we can get much of the value of real-time by rejecting transactions with outdated pricing information
      - This means sending the pricing data with the buy/sell branch
        - Reject transactions with out-of-date pricing

- Frontend
  - Better table formatting
  - Fix navbar taking up too much space on large screen sizes
  - Generally centralise the styling



- Both
  - Transaction-like transactions
  - Figure out why we have more decimal points than we should in the database table

__Post-launch To Do__

- Implement frontend unit testing 
- Frontend: implement password-forgot-emails

- Question: How will future events be handled in a way that is persistent?
    - Storage is handled by the Markets table
    - Every 30 minutes, search the table and determine if a market is going to expire in the next 30 minutes or if it has already expired (think: app crashing right before market resolves, want to handle this accordingly)
        - There are many ways to think about this but every single one depends on a time-invariant resolution method making changes to the database


The Future
- Prediction contests
- USDC for real money _if and only if_ it is legal
