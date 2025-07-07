# Welcome to IMDB Graph

This Repo contains all the code for [https://www.imdbgraph.org](https://www.imdbgraph.org). The website
is written in React/Typescript and uses Next.js as the main framework.

## How to run locally

### Dev setup

1. Install NVM.
   - Windows: https://github.com/coreybutler/nvm-windows
   - Linux/macOS: https://github.com/nvm-sh/nvm
2. Install Node.js lts (version 22.x): `nvm install --lts && nvm use --lts`
3. Install pnpm.
   - Windows: `Invoke-WebRequest https://get.pnpm.io/install.ps1 -UseBasicParsing | Invoke-Expression`
   - Linux/macOS: `curl https://get.pnpm.io/install.sh | sh`
4. Install dependencies: `pnpm install`

## Database Setup

This projects relies on a postgres database. To run the app you will need either a local postgres database or a connection string to a remote database.

To setup database credentials, copy the .env.sample -> .env file and replace with your own connection url.

## Run app

Then run the server with: `pnpm run dev`. Open [http://localhost:3000](http://localhost:3000) with your browser to see the website. You can start editing any of the pages. It will auto-update as you edit the file without having to restart the server or refresh the browser page.

## Linting and Formatting

All linting for this project is done through [ESLint](https://eslint.org/) and all formatting checked
using [Prettier](https://prettier.io/). These rules are also checked through the CI/CD whenever changes are pushed to
Gitlab.

## Testing

To run tests use the command `pnpm test`. This project uses jest and playwright as its testing framework. All tests
located in [tests](./tests) folder. All tests are run through the CI/CD whenever changes are pushed to Gitlab.

## Deployment

All changes pushed to main are automatically deployed to production using Vercel. Any changes to a branch other than
main are deployed to a staging URL for previewing changes before production.
