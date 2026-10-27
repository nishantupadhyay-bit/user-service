export USER_URL=http://localhost:8080
1. Create a user
   curl -i -X POST "$USER_URL/users" \
   -H "Content-Type: application/json" \
   -d '{
   "fullName": "Aarav Sharma",
   "email": "aarav@example.com"
   }'
   Expected: 201 Created with the created user in the response, if the
   controller is configured to return that status.

Save the returned user ID for the next requests.
2. Retrieve a user by ID
   Replace 1 with an existing user ID.
   curl -i "$USER_URL/users/1"
   Expected: 200 OK with the user's details. A missing user should return
   the status configured by the controller, typically 404 Not Found.


3. List users
   curl -i "$USER_URL/users"
   Expected: 200 OK with a JSON array of users.


4. Test duplicate email handling
   Run this after creating a user with aarav@example.com.
   curl -i -X POST "$USER_URL/users" \
   -H "Content-Type: application/json" \
   -d '{
   "fullName": "Another User",
   "email": "aarav@example.com"
   }'
   Expected: a client error if duplicate-email handling is implemented (for
   example, 409 Conflict). The application should remain running.


5. Test invalid email
   curl -i -X POST "$USER_URL/users" \
   -H "Content-Type: application/json" \
   -d '{
   "fullName": "Test User",
   "email": "not-an-email"
   }'
   Expected: a validation error if request validation is enabled (typically
   400 Bad Request).



6. Test a nonexistent user
   curl -i "$USER_URL/users/999999"
   Expected: the not-found status configured by the controller, typically
   404 Not Found.