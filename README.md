BITESPEED BACKEND TASK
Identity Reconciliation

Application is deployed on render via Docker
## Prerequisites
If you testing it locally:

- **Java 17**: Make sure Java 17 is installed on your system.
- **Gradle**: Ensure Gradle is installed and configured.
- **Curl**: For testing API endpoints, ensure `curl` is installed. On Windows, you can use Git Bash which includes `curl`.

POST endpoint (hosted on render.com) : https://bitespeed-e56g.onrender.com/api/contacts/identify

POST endpoint (in Curl command) : curl -X POST -H "Content-Type: application/json" -d '{"email": "test@example.com", "phoneNumber": "1234567890"}' https://bitespeed-e56g.onrender.com/api/contacts/identify

Note-> Please check the command if you are using command prompt.

Database used: PostgreSQL which is also hosted on render.com
Environment variables have been used for the connection to seperate out configuration settings from code.

- `DATABASE_URL`: URL of your PostgreSQL database
- `DATABASE_USERNAME`: Username for your PostgreSQL database
- `DATABASE_PASSWORD`: Password for your PostgreSQL database

If you are testing the application locally, make sure you export them first.

export DATABASE_URL=jdbc:postgresql://dpg-cpdlm7nsc6pc7393s09g-a.oregon-postgres.render.com/contacts_jer0

export DATABASE_USERNAME=contacts_jer0_user

export DATABASE_PASSWORD=dTft2eUgm5j1ZQZgZbrzrvCndJIaIsMw


If you have any queries please reach me out on mail or linkedin:

mail: raigo.ndvinnu@gmail.com

linkedin : www.linkedin.com/in/ganesh-p-raigond-223a141a7



