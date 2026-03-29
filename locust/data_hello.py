import time
import random
import hmac
import hashlib
from locust import HttpUser, task, between

AK = "test-ak"
SK = "test-sk"


class EventReportUser(HttpUser):
    wait_time = between(0.1, 0.5)

    @task
    def hello_event(self):
        with self.client.get("/api/v1/auth/hello") as resp:
            if resp.status_code == 200:
                pass
            else:
                resp.failure(f"Got {resp.status_code}")
