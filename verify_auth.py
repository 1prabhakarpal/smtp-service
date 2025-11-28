import urllib.request
import urllib.parse
import json
import base64

API_URL = "http://localhost:8095/api"
USER_EMAIL = "auth_test@example.com"
USER_PASSWORD = "password123"

def make_request(endpoint, method="POST", data=None):
    url = f"{API_URL}{endpoint}"
    headers = {'Content-Type': 'application/json'}
    if data:
        data = json.dumps(data).encode('utf-8')
    
    try:
        req = urllib.request.Request(url, data=data, headers=headers, method=method)
        with urllib.request.urlopen(req) as response:
            return response.status, json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')

def decode_jwt(token):
    parts = token.split('.')
    if len(parts) != 3:
        return None
    payload = parts[1]
    padded = payload + '=' * (4 - len(payload) % 4)
    return json.loads(base64.urlsafe_b64decode(padded).decode('utf-8'))

def run_test():
    print("--- Testing Auth Service ---")

    # 1. Register
    print("1. Registering...")
    status, body = make_request("/register", data={"username": USER_EMAIL, "password": USER_PASSWORD})
    if status == 200:
        print("   Success.")
    elif status == 400 and "already exists" in str(body):
        print("   User already exists.")
    else:
        print(f"   Failed: {status} - {body}")
        return

    # 2. Login Success
    print("2. Logging in (Correct Credentials)...")
    status, body = make_request("/login", data={"username": USER_EMAIL, "password": USER_PASSWORD})
    if status == 200:
        token = body.get("token")
        print(f"   Success. Token: {token[:20]}...")
        
        # Verify Roles
        claims = decode_jwt(token)
        print(f"   Claims: {claims}")
        if claims and claims.get("roles") == "USER":
            print("   Roles verified: USER")
        else:
            print("   FAILED: Roles missing or incorrect.")
    else:
        print(f"   Failed: {status} - {body}")

    # 3. Login Failure
    print("3. Logging in (Incorrect Credentials)...")
    status, body = make_request("/login", data={"username": USER_EMAIL, "password": "wrongpassword"})
    if status == 401:
        print("   Success: Got 401 as expected.")
    else:
        print(f"   Failed: Expected 401, got {status} - {body}")

if __name__ == "__main__":
    run_test()
