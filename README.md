**README**

This was a project to implement a fake-money prediction market, but halfway through I decided I didn't want to write a worse version of [Manifold Markets](https://manifold.markets/). 

**Development Concerns**

- Running the fronted locally just requires Vite

  - `npm run prod-backend` uses the backend served at `api.humblemarket.iainschmitt.com` and serves the frontend on `localhost`

- Running the backed locally requires JDK >= 17 and MongoDB daemon `mongod` >= 5.0.23
