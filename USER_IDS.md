# Developer User IDs

Each developer should configure their own unique user ID to test as different people in the app.

## Setup Instructions

1. Copy the sample config:
   ```bash
   cp app/src/main/res/raw/user_config_sample.json app/src/main/res/raw/user_config.json
   ```

2. Edit `user_config.json` with your assigned ID below

3. Rebuild the app

## Assigned User IDs

| Developer | User ID | Status |
|-----------|---------|--------|
| Developer 1 | `test-user-dev1-12345` | ✅ Active |
| Developer 2 | `test-user-dev2-12345` | - |
| Developer 3 | `test-user-dev3-12345` | - |
| Developer 4 | `test-user-dev4-12345` | - |
| Developer 5 | `test-user-dev5-12345` | - |

## Example Configurations

### Developer 1 (Alice)
```json
{
  "userId": "test-user-alice-12345",
  "userName": "Alice"
}
```

### Developer 2 (Bob)
```json
{
  "userId": "test-user-bob-12345",
  "userName": "Bob"
}
```

### Developer 3 (Charlie)
```json
{
  "userId": "test-user-charlie-12345",
  "userName": "Charlie"
}
```

## Testing Multi-User Scenarios

With different user IDs, you can test:

1. **Check-ins at the same location**
   - Developer 1 checks in at Coffee Shop
   - Developer 2 checks in at the same Coffee Shop
   - Both should see each other's profiles

2. **Swipes and Matches**
   - Developer 1 swipes right on Developer 2
   - Developer 2 swipes right on Developer 1
   - Both should get a match notification

3. **Messaging**
   - After matching, test the messaging feature
   - Each developer sees their own chat history

## Creating Test Profiles

After setting your user ID, run the setup script to create your profile:

```bash
node setup-my-profile.js
```

This will create a user profile in the database with your configured user ID.

## Important Notes

- **Never commit** `user_config.json` to git (it's in `.gitignore`)
- Each developer can change their user ID at any time
- Use the same API key (shared backend access)
- User IDs must be unique per developer
- Format: `test-user-YOURNAME-12345`

## Troubleshooting

**Issue: "User not found"**
- Run `node setup-my-profile.js` to create your profile in the database

**Issue: "Check-in already exists"**
- Run `node remove-all-my-checkins.js` to clear your check-ins

**Issue: "Seeing wrong user's data"**
- Check that your `user_config.json` has the correct userId
- Rebuild and reinstall the app
