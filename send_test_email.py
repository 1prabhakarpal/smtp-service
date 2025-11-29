import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

def send_email():
    sender_email = "test@devprabhakar.in"
    receiver_email = "prabhakar.ms.pal@gmail.com"
    password = "password"  # Assuming no auth or simple auth for now

    message = MIMEMultipart("alternative")
    message["Subject"] = "Test Email from SMTP Service"
    message["From"] = sender_email
    message["To"] = receiver_email

    text = """\
    Hi,
    This is a test email sent from your self-hosted SMTP server on EC2.
    """
    part1 = MIMEText(text, "plain")
    message.attach(part1)

    try:
        # Connect to localhost:25000 (tunneled to server:25000)
        with smtplib.SMTP("localhost", 25000) as server:
            server.set_debuglevel(1)
            # server.starttls() # Optional depending on config
            # server.login(sender_email, password)
            server.sendmail(sender_email, receiver_email, message.as_string())
        print("Email sent successfully!")
    except Exception as e:
        print(f"Error sending email: {e}")

if __name__ == "__main__":
    send_email()
