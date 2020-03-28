from abc import ABC
from tornado.web import RequestHandler


class IndexHandler(RequestHandler, ABC):
    def get(self):
        self.write("Hello")
