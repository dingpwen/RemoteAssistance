from abc import ABC
from handlers.base.BaseHandler import BaseHandler
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
        user_array.append(user_json)
    return user_array


class FriendHandler(BaseHandler, ABC):
    @check_token
    async def get(self, *args, **kwargs):
        token = self.get_query_argument("token")
        url = self.request.uri
        if url.startswith("/friend/list"):
            await self.get_friend_list(token)
            return
        else:
            await self.send_invalid_path()

    @check_token
    async def post(self, *args, **kwargs):
        url = self.request.uri
        token = self.get_body_argument('token')
        ftk = self.get_body_argument('goal_token')
        print(token, ftk)
        if url.startswith("/friend/add"):
            await self.add_friend(token, ftk)
            return
        elif url.startswith("/friend/del"):
            await FriendModel.del_friend(token, ftk)
            result = dict()
            result["status"] = 200
            result["msg"] = "add success"
            self.write(json.dumps(result))
        else:
            await self.send_invalid_path()

    async def get_friend_list(self, token):
        result = dict()
        friends = FriendModel.get_friends(token)
        if friends is None:
            result["status"] = 201
            result["msg"] = "no friend"
        else:
            result["status"] = 200
            result["friends"] = use_to_json(friends)
        print("result:", json.dumps(result))
        self.write(json.dumps(result))

    async def add_friend(self, token, friend_token):
        res, friend = FriendModel.add_friend(token, friend_token)
        result = dict()
        if res == 0:
            result["status"] = 200
            result["msg"] = "add success"
        else:
            result["status"] = res
            result["msg"] = "add failed"
        self.write(json.dumps(result))
