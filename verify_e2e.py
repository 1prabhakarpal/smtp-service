import smtplib
import ssl
import urllib.request
import urllib.parse
import json
import time
import sys

# Configuration
API_URL = "http://localhost:8095/api"
SMTP_HOST = "localhost"
SMTP_PORT = 25000
USER_EMAIL = "e2e@example.com"
USER_PASSWORD = "password123"
SENDER_EMAIL = "sender@example.com"
EMAIL_SUBJECT = "E2E Test Email"
EMAIL_BODY = "This is a test email sent during end-to-end verification."
LOG_FILE = "e2e_test.log"

def log(message):
    timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
    log_msg = f"[{timestamp}] {message}"
    print(log_msg)
    with open(LOG_FILE, "a") as f:
        f.write(log_msg + "\n")

def make_api_request(endpoint, method="GET", data=None, token=None):
    url = f"{API_URL}{endpoint}"
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    
    if data:
        data = json.dumps(data).encode('utf-8')
    
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            return response.status, response.read().decode('utf-8')
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode('utf-8')
    except Exception as e:
        log(f"API Request Error: {e}")
        return None, None

def send_email_smtp():
    log("Sending email via SMTP...")
    try:
        server = smtplib.SMTP(SMTP_HOST, SMTP_PORT)
        server.set_debuglevel(0) # Set to 1 for verbose SMTP logs
        
        server.ehlo()
        if server.has_extn("STARTTLS"):
            log("SMTP: Switching to STARTTLS")
            context = ssl.create_default_context()
            context.check_hostname = False
            context.verify_mode = ssl.CERT_NONE
            server.starttls(context=context)
            server.ehlo()
        
        msg = f"Subject: {EMAIL_SUBJECT}\n\n{EMAIL_BODY}"
        server.sendmail(SENDER_EMAIL, [USER_EMAIL], msg)
        server.quit()
        log("SMTP: Email sent successfully.")
        return True
    except Exception as e:
        log(f"SMTP Error: {e}")
        return False

def run_e2e_test():
    # Clear log file
    with open(LOG_FILE, "w") as f:
        f.write("Starting E2E Test\n")

    # 1. Register
    log("1. Registering User...")
    status, body = make_api_request("/auth/register", "POST", {"username": USER_EMAIL, "password": USER_PASSWORD})
    if status == 200:
        log("   Registration successful.")
    elif status == 400 and "already exists" in body:
        log("   User already exists, proceeding.")
    else:
        log(f"   Registration failed: {status} - {body}")
        return

    # 2. Login
    log("2. Logging In...")
    status, body = make_api_request("/auth/login", "POST", {"username": USER_EMAIL, "password": USER_PASSWORD})
    token = None
    if status == 200:
        token = json.loads(body).get("token")
        log("   Login successful. Token obtained.")
    else:
        log(f"   Login failed: {status} - {body}")
        return

    # 3. Send Email
    log("3. Sending Email via SMTP...")
    if not send_email_smtp():
        log("   Failed to send email.")
        return

    # Wait for persistence
    log("   Waiting 2 seconds for persistence...")
    time.sleep(2)

    # 4. Verify Email in Inbox
    log("4. Checking Inbox via API...")
    status, body = make_api_request(f"/emails?recipient={USER_EMAIL}", "GET", token=token)
    if status == 200:
        emails = json.loads(body)
        log(f"   Emails received: {len(emails)}")
        found = False
        for email in emails:
            if email.get("subject") == EMAIL_SUBJECT and email.get("body") == EMAIL_BODY:
                found = True
                break
        
        if found:
            log("   SUCCESS: Test email found in inbox!")
        else:
            log("   FAILURE: Test email NOT found in inbox.")
            log(f"   Emails: {emails}")
    else:
        log(f"   Failed to fetch emails: {status} - {body}")

if __name__ == "__main__":
    run_e2e_test()
