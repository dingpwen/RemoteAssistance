from abc import ABC
from tornado.web import RequestHandler


class FriendHandler(RequestHandler, ABC):
    def get(self, *args, **kwargs):
        token = self.get_query_argument("token")
        print("token:", token)
        print("url:", self.request.uri)
        pass

    def post(self):
        pass
