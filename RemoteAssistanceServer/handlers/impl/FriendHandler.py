from abc import ABC
from tornado.web import RequestHandler
from models.friend import FriendModel
from libs.hooks import check_token
from models.user import User
import json


def use_to_json(users):
    user_array = []
    for user in users:
        user_json = dict()
        user_json["name"] = user.name
        user_json["user_token"] = user.token
        user_json["imageUrl"] = user.image
        user_array.append(json.dumps(user_json))
    return json.dumps(user_array)


class FriendHandler(RequestHandler, ABC):
    @check_token
    async def get(self, *args, **kwargs):
        token = self.get_query_argument("token")
        url = self.request.uri
        print("token:", token)
        print("url:", url)
        result = dict()
        if url.startswith("/friend/list"):
            friends = await FriendModel.get_friends(token)
            if friends is None:
                result["status"] = 201
                result["msg"] = "no friend"
            else:
                result["status"] = 200
                result["friends"] = use_to_json(friends)
            return json.dumps(result)
        else:
            pass
        self.write('invalid path')
        self.send_error(404)

    @check_token
    async def post(self):
        url = self.request.uri
        token = self.get_body_argument('token')
        ftk = self.get_body_argument('goal_token')
        print(token, ftk)
        result = dict()
        if url.startswith("/friend/add"):
            res, friend = await FriendModel.add_friend(token, ftk)
            if res == 0:
                result["status"] = 200
                result["msg"] = "add success"
            else:
                result["status"] = res
                result["msg"] = "add failed"
            return json.dumps(result)
        elif url.startswith("/friend/del"):
            await FriendModel.del_friend(token, ftk)
            result["status"] = 200
            result["msg"] = "add success"
            return json.dumps(result)
        else:
            pass
        self.write('invalid path')
        self.send_error(404)
