from abc import ABC
from tornado.web import RequestHandler


class BaseHandler(RequestHandler, ABC):
    async def send_invalid_path(self):
        self.write('invalid path')
        self.send_error(404)
