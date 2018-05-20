import random

import time
from concurrent import futures

import grpc
from threading import Thread

import numpy as np

from generated.currency_pb2_grpc import ExchangeServiceServicer
from generated import currency_pb2_grpc
from generated.currency_pb2 import CurrencyExchange, USD, PLN, CHF, EUR, CurrencyType, Currencies

possible = np.zeros((150, 4))
possible[0:, 0] = 1
possible[0:, 1] = np.random.uniform(1.50, 3.95, size=150)
possible[0:, 2] = np.random.uniform(3.90, 4.50, size=150)
possible[0:, 3] = np.random.uniform(3.00, 4.50, size=150)


def get_random_rate(start, finish):
    return start + random.random() * (finish - start)


def set_currency_exchange(currency_type, rate):
    exchange = CurrencyExchange()
    exchange.exchangeRate = rate
    exchange.type = currency_type
    return exchange


def change_exchange_rate():
    while True:
        random_currency = random.randint(1, 3)
        CurrencyServer.rates[random_currency].exchangeRate \
            = possible[random.randint(0, 149), random_currency]
        time.sleep(7)


class CurrencyServer(ExchangeServiceServicer):
    rates = {USD: set_currency_exchange(USD, get_random_rate(3.0, 4.50)),
             PLN: set_currency_exchange(PLN, 1),
             CHF: set_currency_exchange(CHF, get_random_rate(1.50, 3.95)),
             EUR: set_currency_exchange(EUR, get_random_rate(3.90, 4.50))}

    def __init__(self):
        self.updated_currency = None

    def getCurrencyRate(self, request, context):
        handled_currencies = []
        print(request)
        for currency in request.type:
            handled_currencies.append(currency)
            yield CurrencyServer.rates[currency]
        while True:
            for currency in request.type:
                handled_currencies.append(currency)
                yield CurrencyServer.rates[currency]
            time.sleep(12)


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    currency_pb2_grpc.add_ExchangeServiceServicer_to_server(
        CurrencyServer(), server)
    server.add_insecure_port('[::]:50051')
    server.start()
    try:
        while True:
            time.sleep(250000)
    except KeyboardInterrupt:
        server.stop(0)


Thread(target=change_exchange_rate).start()
serve()
