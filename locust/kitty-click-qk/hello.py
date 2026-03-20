import hashlib
import hmac
import time

from locust import HttpUser, task


class QuickUser(HttpUser):

    @task(10)
    def report_login(self):
        resp = self.client.get("/runtime/hello")
