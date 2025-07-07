# Welcome to IMDB Graph's frontend

This Repo contains all the code for [https://www.imdbgraph.org](https://www.imdbgraph.org). The website
is written in React/Typescript and uses Next.js as the main framework.

## How to run locally

To run the website locally, first install pnpm. Once installed, run the development server using the following command:

```bash
pnpm run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the website.

You can start editing any of the pages. It will auto-update as you edit the file without having to restart the server or
refresh the browser page.

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
