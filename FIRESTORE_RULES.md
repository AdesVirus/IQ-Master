# Firestore Security Rules

This document contains the recommended Firestore security rules for the IQ Master app.

## Rules Configuration

Apply these rules in your Firebase Console under Firestore Database → Rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Default deny all
    match /{document=**} {
      allow read, write: if false;
    }
    
    // Users can only access their own data
    match /users/{userId} {
      // Allow read/write only if authenticated and user ID matches
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Scores collection under user document
      match /scores/{scoreId} {
        // Allow creating new scores
        allow create: if request.auth != null 
                      && request.auth.uid == userId
                      && request.resource.data.keys().hasAll(['score', 'localTimestamp'])
                      && request.resource.data.score is int
                      && request.resource.data.score >= 0
                      && request.resource.data.score <= 100;
        
        // Allow reading own scores
        allow read: if request.auth != null && request.auth.uid == userId;
        
        // Allow deleting own scores
        allow delete: if request.auth != null && request.auth.uid == userId;
        
        // Prevent updates to existing scores
        allow update: if false;
      }
      
      // Consent records collection under user document
      match /consent/{consentId} {
        // Allow creating consent records
        allow create: if request.auth != null 
                      && request.auth.uid == userId
                      && request.resource.data.keys().hasAll(['consentType', 'consentStatus']);
        
        // Allow reading own consent records
        allow read: if request.auth != null && request.auth.uid == userId;
        
        // Prevent updates and deletes to consent records (audit trail)
        allow update, delete: if false;
      }
    }
  }
}
```

## Rules Explanation

### User Data Isolation
- Each user's data is stored under `/users/{userId}`
- Users can only access data where `userId` matches their Firebase Auth UID
- This prevents users from accessing or modifying other users' data

### Scores Collection
- **Create**: Users can create new score documents with valid data
  - Score must be an integer between 0 and 100
  - Required fields: `score`, `localTimestamp`
- **Read**: Users can read all their own scores
- **Delete**: Users can delete their own scores (for the "Clear Cloud Scores" feature)
- **Update**: Updates are prevented to maintain score integrity

### Consent Collection
- **Create**: Users can create consent audit records
  - Required fields: `consentType`, `consentStatus`
- **Read**: Users can read their own consent history
- **Update/Delete**: Not allowed (maintains audit trail)

## Testing Rules

Use the Firebase Console Rules Playground to test:

1. Go to Firestore Database → Rules → Rules Playground
2. Select "Get" operation
3. Location: `/users/test-user-id/scores/score-1`
4. Auth provider: Firebase Authentication
5. Auth UID: `test-user-id`
6. Click "Run"

Expected: ✅ Allow (if UID matches)
Expected: ❌ Deny (if UID doesn't match)

## Data Structure

### Score Document
```
/users/{userId}/scores/{scoreId}
  - score: number (0-100)
  - localTimestamp: number (timestamp in ms)
  - timestamp: timestamp (server timestamp, optional)
```

### Consent Document
```
/users/{userId}/consent/{consentId}
  - consentType: string ("personalized" | "non_personalized" | "no_ads")
  - consentStatus: string ("Unknown" | "Required" | "Obtained" | "Not Required")
  - timestamp: timestamp (server timestamp, optional)
```

## Security Best Practices

1. **Never use admin access in client apps** - Always authenticate users
2. **Validate all data** - Check types and ranges in security rules
3. **Prevent unauthorized access** - Use auth checks in all rules
4. **Maintain audit trails** - Prevent deletion of important records
5. **Test thoroughly** - Use the Rules Playground before deploying

## Common Issues

### "Permission Denied" Errors

If users get permission denied errors:

1. Check that Firebase Authentication is working (user is signed in anonymously)
2. Verify the user ID in the document path matches the authenticated UID
3. Check that required fields are present in write operations
4. Review Firestore rules in console
5. Check Logcat for detailed error messages

### Rules Not Taking Effect

If rules don't seem to work:

1. Click "Publish" in the Firebase Console
2. Wait a few seconds for rules to propagate
3. Clear app data and restart
4. Check for typos in collection/document paths

## Monitoring

Monitor rule usage in Firebase Console:

1. Go to Firestore Database → Usage
2. Check for spike in denied reads/writes
3. Review logs for security rule violations
4. Adjust rules based on legitimate access patterns

## Production Checklist

Before launching to production:

- [ ] Deploy rules to Firebase Console
- [ ] Test rules with real user accounts
- [ ] Monitor denied requests for issues
- [ ] Set up alerts for unusual activity
- [ ] Document any custom rules added
- [ ] Review rules quarterly for updates

## Additional Resources

- [Firestore Security Rules Documentation](https://firebase.google.com/docs/firestore/security/get-started)
- [Security Rules Best Practices](https://firebase.google.com/docs/firestore/security/rules-conditions)
- [Testing Rules](https://firebase.google.com/docs/firestore/security/test-rules-emulator)
