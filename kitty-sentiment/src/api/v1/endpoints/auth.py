"""
简易登录（演示环境）；生产环境请接入真实认证。
"""

import hashlib
import hmac
import os
import time

from flask import request, jsonify

from src.api.v1 import api_v1
from src.schemas import LoginRequest, LoginResponse


def _sign_token(username: str, ts: int) -> str:
    secret = os.environ.get("APP_AUTH_SECRET", "kitty-sentiment-dev-secret")
    msg = f"{username}:{ts}".encode()
    return hmac.new(secret.encode(), msg, hashlib.sha256).hexdigest()


@api_v1.route("/auth/login", methods=["POST"])
def login():
    try:
        body = LoginRequest(**(request.get_json() or {}))
    except Exception as e:
        return jsonify({"error": str(e)}), 400

    user = os.environ.get("APP_DEMO_USER", "admin")
    pwd = os.environ.get("APP_DEMO_PASSWORD", "admin")
    if body.username != user or body.password != pwd:
        return jsonify({"error": "用户名或密码错误"}), 401

    ts = int(time.time())
    sig = _sign_token(body.username, ts)
    token = f"{body.username}.{ts}.{sig}"

    return jsonify(LoginResponse(token=token, expires_in=86400).model_dump(mode="json", by_alias=True))
