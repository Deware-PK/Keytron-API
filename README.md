# 🔑 Keytron API

**Keytron API** is a demo project built with **Spring Boot** and **MongoDB** that implements a simple but extensible **software license management system**.  
It is designed for educational purposes, showcasing how to build a backend API for generating, activating, validating, and managing product licenses.

---

## ✨ Features

- **Admin API**
  - Generate new license keys with product, plan, seats, and expiry.
  - Secure with `X-API-Key` header.
  - Store licenses in MongoDB with audit logs.

- **Client API**
  - **Activate** a license with hardware/device fingerprint validation.
  - **Validate** token (JWT-based).
  - **Heartbeat** mechanism to automatically expire inactive activations.
  - **Deactivate** activations to free up seats.
  - Enforce constraints: license expiry, ban/revoke, seat limit, HWID mismatch.

- **Audit Logging**
  - Records every event (activation, heartbeat, validation, deactivation, failure reasons).
  - Useful for debugging and tracking.

---

## 🚀 Tech Stack

- Java 17+
- Spring Boot
- Spring Security (API Key + JWT)
- MongoDB
- Maven

---

## ⚙️ Configuration

Create a file `application-example.properties` and replace values with your own secrets.  
⚠️ **Never commit real secrets or production credentials to GitHub.**

```properties
# Server
server.port=8081

# MongoDB
spring.data.mongodb.uri=mongodb://localhost:27017/KeytronDB

# Admin API key (used for /v1/admin/... endpoints)
app.admin.key=change-me

# HMAC secret for license hashing
# If you want to use hex bytes, prefix with "hex:"
app.secret=hex:deadbeefcafebabe1234567890abcdef

# Heartbeat TTL (minutes) - how long an activation is valid without refresh
app.license.activation.heartbeatTtlMinutes=60 
```

---

## 🔒 Admin API

➤ Generate License

`POST /v1/admin/licenses/generate`

- **Headers**
```
X-API-Key: <admin-secret>
Content-Type: application/json
```
- **Body**
```
{
"productCode": "PRO",
"plan": "PRO",
"seats": 1,
"expiresAt": "2026-12-31T00:00:00Z"
}
```
- **Response**
```
{
"licenseId": "66f1abc123...",
"licenseKeyPlain": "PRO-ABCD-EFGH-IJKL-1234"
}
```
---

➤ Ban License  

`POST /v1/admin/licenses/ban`

- **Headers**
```
X-API-Key: <admin-secret>
Content-Type: application/json
```

- **Body**
  
```
{
"licenseId": "66f1abc123...",
"reason": "Suspicious activity"
}

```
- **Response**
```
{
"status": "BANNED",
"licenseId": "66f1abc123...",
"reason": "Suspicious activity"
}
```
---

## 🖥 Client API

---

➤ Activate License

`POST /v1/licenses/activate`

- **Body**
  
```
{
"licenseKey": "PRO-XXXX-YYYY",
"deviceFingerprint": "Client_HWID",
"os": "Windows 11",
"hostname": "MYPC",
"appVersion": "1.0.0",
"ip": "127.0.0.1",
"userAgent": "..."
}

```
- **Response**
```
{
"activationId": "66f1...",
"token": "eyJhbGciOi...",
"plan": "PRO",
"seats": 1,
"expiresAt": "2026-12-31T00:00:00Z"
}
```

➤ Validate Token

`GET /v1/licenses/validate`

- **Headers**
```
Authorization: Bearer <token>
```

- **Response**
```
{
"valid": true,
"error": null,
"ts": "2025-08-25T00:30:00Z"
}
```
---

➤ Heartbeat

`POST /v1/licenses/heartbeat`

- **Body**
  
```
{
"activationId": "66f1..."
}

```
- **Response**
```
200 OK
```
---
➤ Deactivate

`POST /v1/licenses/deactivate`

- **Body**
  
```
{
"activationId": "66f1..."
}

```
- **Response**
```
200 OK
```
---
## 📝 Audit Log

- **Every action is recorded in MongoDB (auditlogs collection):**
  
  - ACTIVATE_OK / ACTIVATE_FAIL
  - HEARTBEAT
  - VALIDATE
  - DEACTIVATE
    
---
## ⚠️ Security Notice

This project is intended for demo/educational purposes only.
Do not use it in production as-is.

- Keep app.secret and app.admin.key out of source control.

- Do not commit application.properties with real secrets.

- Use environment variables or secret managers for deployment.

---
