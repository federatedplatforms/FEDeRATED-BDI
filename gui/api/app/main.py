from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
from pydantic import BaseModel
import requests
import json

from datetime import datetime, timedelta
from typing import Optional

import os


origins = [
    "http://localhost",
    "http://localhost:8080",
    "http://127.0.0.1:8080",
    "*",  # for DEV, remove in production
]

FEDERATED_ENDPOINT = "https://fenix-codognotto.azurewebsites.net/api/GetOrderEvent"  # refer to docker container that holds ES database

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# get events from BDI endpoint
@app.get("/events")
def get_events(company: str, year: str, number: str):
    url = f"{FEDERATED_ENDPOINT}/{company}/{year}/{number}"
    reply = requests.get(url)
    return json.loads(reply.text)
