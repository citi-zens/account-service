# 🏦 Bank Service — Unintended Side-Effect Vulnerability Demo

This Spring Boot application demonstrates a subtle but critical security flaw: an **unintended side-effect** caused by an optional, unused API field that triggers an unauthorized database action. This leads to **silent data pollution** and highlights why strict DTO boundaries and service-layer validation are essential in secure backend design.

---

## ⚠️ Vulnerability Type: Unintended Side-Effect / Data Pollution

The vulnerability lives inside the `UpdateBalanceRequest` DTO of the `/api/accounts/update` endpoint.

A hidden field — **`optionalNewUserEmail`** — is:

- Not part of the intended balance update request  
- Completely optional  
- Ignored at the controller layer  
- **BUT** inspected inside the service layer  

When present, it triggers:

- Unauthorized creation of a **new user**
- Silent data insertion without audit trail
- Pollution of the internal H2 database
- Misleading results when calling the GET API  
- A backdoor that attackers can easily abuse

This is a real-world example of how **unused, undocumented, or “harmless” fields can create hidden attack surfaces**.

---

## 🚀 Getting Started

### **Prerequisites**

- Java 17+
- Apache Maven
- cURL (for API testing)

---

### **1. Run the Application**

```bash
./mvnw spring-boot:run
```

The application will start on ```http://localhost:8080. ```

## ⚙️ API Endpoints 
The application exposes three primary endpoints, all interacting with the internal H2 in-memory database:

| # | HTTP Method | Path | Description | 
|------|-----|-------|--------|
| 1 | POST | ``/api/accounts/create`` | Creates a new user account. | 
| 2 | GET | ``/api/accounts/{email}`` | Retrieves the balance and details for a specific user. | 
| 3 | PUT | ``/api/accounts/update`` | VULNERABLE. Updates the user's balance based on request body data. |


## 😈 Exploitation Steps (Triggering the Vulnerability)

The vulnerability is triggered by sending the secret optionalNewUserEmail field in the body of the PUT ``/api/accounts/update`` request.

**Step 1: Create a Legitimate Target User**

We need a valid account to submit the malicious update request against.

```
# Create user "legit@bank.com" with a starting balance of 1000.0

curl -X POST "http://localhost:8080/api/accounts/create?email=legit@bank.com&initialBalance=1000.0"
```

**Step 2: Exploit the Unintended Side-Effect**

We use the UPDATE endpoint to simultaneously change the legitimate user's balance and inject a rogue user into the database via the side-channel field.

```
# VULNERABLE QUERY: Updates legit@bank.com's balance AND secretly creates rogue@attacker.com

curl -X PUT "http://localhost:8080/api/accounts/update" \
-H "Content-Type: application/json" \
-d '{
  "email": "legit@bank.com",
  "amount": 550.0,
  "optionalNewUserEmail": "rogue@attacker.com" 
}'

```

In the server console logs, you will see the output confirming the exploit:
``!!! VULNERABILITY TRIGGERED: Creating rogue user: rogue@attacker.com``

**Step 3: Verify the Data Pollution**

We check if the rogue user, who was never created via the official /create API, now exists in the database.
```
# Query the database for the rogue user using the GET endpoint

curl -X GET "http://localhost:8080/api/accounts/rogue@attacker.com"

```


Expected Result: The server will return a 200 OK response with the ``rogue@attacker.com`` user details, demonstrating that an unauthorized data operation occurred through a hidden side-channel.
```
{
  "id": 2, 
  "email": "rogue@attacker.com", 
  "balance": 0.0
}
```




## 🛡️ Remediation

To fix this vulnerability, the principle of Separation of Concerns must be strictly applied.

Remove the unauthorized field from the ``UpdateBalanceRequest`` DTO.

Refactor the ``AccountService.updateBalance()`` method to only handle balance updates and remove all logic related to user creation (e.g., remove the if ``(rogueEmail != null...)`` block).

Ensure that any sensitive business logic, such as user provisioning, can only be accessed through explicitly authorized and validated endpoints.
