# Eval Suite

After downloading the project do these things in order:

1. Run this command:

   ```sh
   cp eval-suite-backend/.env.example eval-suite-backend/.env
   ```

2. Fill in your environment variables in the newly created `.env` file.

   > TU Wien Aqueduct works with the model and url already specified, only the api key is needed.

3. There are 2 options for running the project:

   ### Docker only (no mise required)

   With Docker running, build and start the whole stack in one command:

   ```sh
   docker compose up --build
   ```

   | Service  | URL                     |
   | -------- | ----------------------- |
   | Frontend | http://localhost:4000   |
   | Backend  | http://localhost:8080   |
   | Postgres | localhost:5433          |

   ### mise (local dev with hot reload)

   If you have mise-en-place installed run:

   ```sh
   mise trust && mise install && mise dev
   ```
