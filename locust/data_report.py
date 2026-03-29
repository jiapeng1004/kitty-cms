import time
import random
import hmac
import hashlib
from locust import HttpUser, task, between

AK = "test-ak"
SK = "test-sk"

def compute_signature(secret_key, ak, timestamp):
    message = ak + str(timestamp)
    h = hmac.new(secret_key.encode(), message.encode(), hashlib.sha256)
    return h.hexdigest()

class EventReportUser(HttpUser):
    wait_time = between(0.1, 0.5)

    def on_start(self):
        timestamp = int(time.time())
        sign = compute_signature(SK, AK, timestamp)
        self.headers = {
            "X-AK": AK,
            "X-SIGN": sign,
            "X-TIMESTAMP": str(timestamp),
            "Content-Type": "application/json"
        }

    @task
    def report_event(self):
        event_types = ["login", "upload", "download", "delete", "cpu_usage", "memory_usage"]
        operators = ["user001", "user002", "user003", "system", "admin"]

        payload = {
            "eventType": random.choice(event_types),
            "operator": random.choice(operators),
            "value": random.choice([1, 1, 1, random.randint(-100, 100)])
        }

        with self.client.post("/api/v1/events", json=payload, headers=self.headers, catch_response=True) as resp:
            if resp.status_code == 200:
                resp.success()
            else:
                resp.failure(f"Got {resp.status_code}")
