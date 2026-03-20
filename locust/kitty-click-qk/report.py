import hashlib
import hmac
import time

from locust import HttpUser, task


class QuickUser(HttpUser):
    ak = 'root'
    sk = '123456'

    def gen_sign_headers(self):
        timestamp = int(time.time() * 1000)
        sign_str = f'{self.ak}|{timestamp}'
        sign = hmac.new(self.sk.encode(), sign_str.encode(), hashlib.sha256).hexdigest()
        return {
            'X-AK': self.ak,
            'X-SIGN': sign,
            'X-TS': str(timestamp)
        }

    @task(10)
    def report_login(self):
        resp = self.client.post("/v1/report", json={
            "operateType": "LOGIN",
            "operator": "locust",
            "value": 1,
        }, headers=self.gen_sign_headers())