# Create users
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{ "username": "alice", "password": "test12345678", "displayName": "Alice" }'

curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{ "username": "bob", "password": "test12345678", "displayName": "Bob" }'
```