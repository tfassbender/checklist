# Implementation Notes

## Architecture Decisions

### File-Based Storage
- User data stored in `data/users/{username}/`
- Each user has a `user.json` for credentials and a `lists/` directory
- Lists stored as individual JSON files (`{uuid}.json`)
- Benefits: Simple, no database setup, easy to backup/inspect

### JWT Authentication
- Tokens valid for 24 hours
- RSA keys generated on first startup if not present
- Token stored in localStorage on frontend
- All `/api/lists/**` endpoints require authentication

### Frontend Architecture
- React Context for global state (auth, theme)
- Custom hooks for data fetching and state management
- Tailwind CSS for styling with dark mode support
- @dnd-kit for drag-and-drop reordering

## Key Implementation Details

### List Name Uniqueness
- Enforced case-insensitively per user
- Checked on create and rename operations
- Returns 409 Conflict if duplicate

### Item Ordering
- Items have `orderIndex` field
- Reorder endpoint accepts array of item IDs in new order
- Indices are recalculated to be sequential (0, 1, 2, ...)

### Dark Mode
- Detects system preference on first load
- User can manually toggle
- Preference persisted in localStorage
- Uses Tailwind's `dark:` variant classes

### Mobile Optimizations
- Minimum tap target size: 44x44px
- Bottom action bar for easy thumb access
- Touch-friendly drag handles
- Responsive grid layout for list cards

## Security Considerations

### Password Storage
- Passwords hashed with bcrypt (via jBCrypt library)
- Salt automatically generated per password
- Hash stored in user.json

### JWT Security
- Tokens signed with RSA private key
- Public key used for verification
- Keys should be securely managed in production

### File System Security
- Username validated to prevent path traversal
- Only alphanumeric characters and underscores allowed
- List IDs are UUIDs (safe for filenames)

## Known Limitations

1. **No concurrent access handling**: File operations are not atomic
2. **No pagination**: All lists loaded at once
3. **No sharing**: Lists are private to each user
4. **No offline support**: Requires server connection

## Future Improvements

- [ ] Add list sharing between users
- [ ] Implement pagination for large list collections
- [ ] Add offline support with service worker
- [ ] Add list templates
- [ ] Add due dates and reminders
- [ ] Add search functionality
