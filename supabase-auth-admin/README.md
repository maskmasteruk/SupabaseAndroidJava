# Supabase Auth Admin Java SDK for Android

A production-quality Java SDK for Supabase Authentication Administrative operations on Android. This SDK allows you to manage users, generate authentication links, and handle Multi-Factor Authentication (MFA) from an administrative context.

## Purpose
This SDK provides a wrapper around the Supabase GoTrue Admin API, enabling developers to perform administrative tasks directly from an Android environment. **These operations require the Service Role Key.**

## Requirements
*   **Minimum Android SDK:** 24
*   **Java Version:** 11+
*   **Android Studio:** Jellyfish or newer recommended
*   **Gradle:** 8.0+
*   **Permissions:** `android.permission.INTERNET`

## Features
*   **User Management:**
    *   Invite users via email.
    *   Create users with email, phone, and metadata.
    *   Retrieve paginated lists of users.
    *   Get specific user details by ID.
    *   Update user information (email, phone, metadata, ban, role, etc.).
    *   Delete users (soft or hard delete).
*   **Administrative Link Generation:**
    *   Signup links.
    *   Magic links.
    *   Invite links.
    *   Recovery links.
    *   Email change links (current and new).
*   **MFA Management:**
    *   List MFA factors for a user.
    *   Delete MFA factors.

---

## Table of Contents
1. [Installation](#installation)
2. [Quick Start](#quick-start)
3. [Initialization](#initialization)
4. [API Reference](#api-reference)
5. [Models](#models)
6. [Callbacks](#callbacks)
7. [Threading](#threading)
8. [Error Handling](#error-handling)
9. [Package Structure](#package-structure)
10. [Security Best Practices](#security-best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Installation

```kotlin
plugins {
    id("io.github.maskmasteruk.supabase") version "0.0.1"
}

dependencies {
    implementation("io.github.maskmasteruk:supabase-android-core:0.0.1")
    implementation("io.github.maskmasteruk:supabase-android-auth-admin:0.0.1")
}
```

---

## Quick Start

### 1. Initialize Supabase
Before using the Admin SDK, you must initialize the core Supabase SDK with your project URL and Service Role Key.

> **Warning:** The Service Role Key has administrative privileges. Be extremely careful when using it in client-side applications.

```java
SupabaseConfig config = new SupabaseConfig()
    .setProjectUrl("https://your-project-id.supabase.co")
    .setAllowOtherKeys(true) // Required to use Service Role Key
    .setProjectServiceRoleKey("your-service-role-key");

Supabase.initialize(config);
```

### 2. Get Admin Instance
```java
SupabaseAdminAuth adminAuth = SupabaseAdminAuth.getInstance(context);
```

### 3. Create a User
```java
adminAuth.createUser(
    "user@example.com", 
    null, 
    "password123", 
    true, 
    false, 
    null, 
    null, 
    null, 
    new AuthCallback() {
        @Override
        public void onSuccess(SupabaseUser user) {
            // User created successfully
        }

        @Override
        public void onError(SupabaseError error) {
            // Handle error
        }
    }
);
```

---

## Initialization

The SDK is initialized via the `SupabaseAdminAuth.getInstance(Context)` method, which returns a singleton instance. It depends on the global `Supabase` initialization from the `:supabase-core` module.

```java
SupabaseAdminAuth adminAuth = SupabaseAdminAuth.getInstance(getApplicationContext());
```

---

## API Reference

### `SupabaseAdminAuth`

| Method | Description |
| :--- | :--- |
| `inviteUser(String email, String redirectTo, Map<String, Object> userData, AuthCallback callback)` | Sends an invitation email to a user. |
| `createUser(String email, String phone, String password, Boolean emailConfirm, Boolean phoneConfirm, Map<String, Object> userMetadata, Map<String, Object> appMetadata, String banDuration, AuthCallback callback)` | Creates a new user with administrative privileges. |
| `getUsers(int page, int perPage, OnGetUsersCallback callback)` | Retrieves a paginated list of users. |
| `getUser(String userID, AuthCallback callback)` | Retrieves a specific user by their ID. |
| `updateUser(String userID, String email, String phone, String password, Boolean emailConfirm, Boolean phoneConfirm, Map<String, Object> userMetadata, Map<String, Object> appMetadata, String banDuration, String role, Boolean disabled, AuthCallback callback)` | Updates an existing user's information. |
| `deleteUser(String userID, boolean softDelete, OnCompleteCallback callback)` | Deletes a user. |
| `generateLink(LinkType type, String email, String password, String new_email, String redirect_to, Map<String, Object> userData, OnGetLinkCallback callback)` | Generates a specific type of authentication link. |
| `listFactors(String userID, OnGetFactorsCallback callback)` | Retrieves all MFA factors for a specific user. |
| `deleteFactor(String userID, String factorID, OnCompleteCallback callback)` | Deletes a specific MFA factor for a user. |

#### Helper Methods for Link Generation
*   `generateSignupLink(...)`
*   `generateMagicLink(...)`
*   `generateInviteLink(...)`
*   `generateRecoveryLink(...)`
*   `generateEmailChangeCurrentLink(...)`
*   `generateEmailChangeNewLink(...)`

---

## Models

### `SupabaseLink`
Represents a generated authentication link and associated tokens.

*   `getActionLink()`: Returns the full action link URL.
*   `getEmailOtp()`: Returns the email OTP.
*   `getHashedToken()`: Returns the hashed token.
*   `getVerificationType()`: Returns the verification type (e.g., signup, recovery).
*   `getRedirectTo()`: Returns the redirect URL.

---

## Callbacks

### `AuthCallback`
Used for operations that return a single `SupabaseUser`.
```java
void onSuccess(SupabaseUser user);
void onError(SupabaseError error);
```

### `OnGetUsersCallback`
Used for fetching multiple users.
```java
void onSuccess(List<SupabaseUser> users);
void onError(SupabaseError error);
```

### `OnGetLinkCallback`
Used for link generation.
```java
void onSuccess(SupabaseLink link);
void onError(SupabaseError error);
```

### `OnCompleteCallback`
Used for operations that only signify completion (e.g., delete).
```java
void onSuccess();
void onError(SupabaseError error);
```

---

## Threading

All network operations in the SDK are performed asynchronously on a background thread using a single-thread executor. 

*   **Synchronous Methods:** Only `getInstance()` and `Supabase.initialize()` are synchronous.
*   **Asynchronous Methods:** All user management, link generation, and MFA operations.
*   **UI Thread:** Callbacks are **not** guaranteed to run on the main/UI thread. You must use `runOnUiThread` or a similar mechanism if you need to update the UI from a callback.

```java
adminAuth.getUser(userID, new AuthCallback() {
    @Override
    public void onSuccess(SupabaseUser user) {
        runOnUiThread(() -> {
            // Update UI here
        });
    }
    // ...
});
```

---

## Error Handling
The SDK uses `SupabaseError` to communicate failures. Always check the error message and code in your callbacks.

```java
@Override
public void onError(SupabaseError error) {
    Log.e("SupabaseAdmin", "Error: " + error.getMessage());
}
```

---

## Package Structure

```text
supabase-auth-admin/
 ├── Callback/          # Callback interfaces
 ├── Enums/             # Enums like LinkType
 ├── Object/            # Data models (SupabaseLink)
 ├── SupabaseAdminAuth  # Main entry point
 ├── UserService        # Internal user management service
 ├── LinkService        # Internal link generation service
 └── MFAService         # Internal MFA management service
```

---

## Security Best Practices
*   **Never Hardcode Secrets:** Avoid hardcoding the Service Role Key. Use environment variables or secure secret management.
*   **Restricted Access:** The Admin SDK should only be used in parts of the app that are strictly for administrative purposes.
*   **HTTPS:** Ensure your project URL uses the `https` protocol.
*   **Service Role Key Protection:** This key bypasses Row Level Security (RLS). Use with extreme caution.

---

## Troubleshooting

| Problem | Cause | Solution |
| :--- | :--- | :--- |
| `Supabase is not Initialized` | `Supabase.initialize()` was not called. | Call `Supabase.initialize()` before using the Admin SDK. |
| `401 Unauthorized` | Invalid or missing Service Role Key. | Ensure `setProjectServiceRoleKey()` is called and the key is correct. |
| `403 Forbidden` | The key used is not the Service Role Key. | Use the Service Role Key, not the Anon/Publishable key. |
| Network Failures | No internet or blocked by firewall. | Check device connectivity and `INTERNET` permission. |

---

## License
MIT License. Copyright (c) 2026 Udhayakrishna K G.
