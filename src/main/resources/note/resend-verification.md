## Resend Verification Email

Resends the verification email to a user whose account is still pending.

### Endpoint

```http
POST /api/v1/auth/resend-verification?email=phai@example.com
```

| Parameter | Type   | Required | Description                      |
|-----------|--------|----------|----------------------------------|
| `email`   | string | Yes      | Email address of the user account |

### Scenario

| Account status                   | Result                          |
|----------------------------------|---------------------------------|
| Pending                          | New verification email is sent  |
| Already verified                 | No email sent                   |
| Unknown email                    | No email sent                   |
| Active, suspended, or locked     | No email sent                   |

> When a new email is sent, any previous verification link is invalidated and no longer works.
