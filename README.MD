**Project Goal**
- Create a (fake money) perdiction market that allows for public and private perdiction market

**TO DO**
- Backend: Create model for markets, private markets, and user permissions
- Backend: implement market model
- Watch Kevin Powell's tutorials
  - [Flexbox](https://www.youtube.com/watch?v=hwbqquXww-U&list=PL4-IK0AVhVjMSb9c06AjRlTpvxL3otpUd&ab_channel=KevinPowell)
  - [Responsive grid](https://www.youtube.com/watch?v=sKFW3wek21Q&ab_channel=KevinPowell)
- Implement frontend unit testing 
- Hash passwords on both frontend, backend
- Backend: implement market model
- Frontend: implement creation of private markets
- Frontend: implement password-forgot-email
- Frontend + Backend implement logging
- Launch the product

- Open questions
  - Are market openings any different than Vegas line setting?
  - What are automated market makers and would they be helpful
    - https://manifoldmarkets.notion.site/Maniswap-ce406e1e897d417cbd491071ea8a0c39
    - https://docs.gnosis.io/conditionaltokens/docs/introduction3/
  - What Mongodb design patterns are most relevant here? 


- Question: How will future events be handled in a way that is persistent?
    - Storage is handled by the Markets table
    - Every 30 minutes, search the table and determine if a market is going to expire in the next 30 minutes or if it has already expired (think: app crashing right before market resolves, want to handle this accordingly)
        - There are many ways to think about this but every single one depends on a time-invariant resolution method making changes to the database

- Four collections
  - User
    - References to each of the other tables (markets participated in/)
  - Market
  - Transaction
  - Position



- Market making displayed in the analysis directory, we are making money (?)
  - Prevent trades if either Y or N is down to only 2 shares
- Backend model overview
  - User, Markets, and Positions are to be stored in separate collections
  - Markets will contain a _few_ outcomes and transaction proposals in their order books, but these aren't expected to rise without limit  so they will be embedded
  - Positions
    - Positons contain user, market ids so they can be associated to a   given user, market (`Position.userId, Position.marketId`)
    - `Position.find({ userId: userId })`, `Position.find({  })` 
  - Transaction proposals
    - Every single transaction propsal comes from at least one position
    - While it doesn't matter for the purpose of share accounting, it would be _helpful_ to literally map positions to transaction proposals
    - The positions wouldn't be deleted, the owners and priceAtBuy would be uploaded
    - The only way a position can disapear is if the seller delists the position, if the market closes, or if the transaction proposal is accepted, so there shouldn't be situations where the   `private Set<String> positionIds;` points to a nonexistent position


The Future
- Perdiction contests
- USDC for real money