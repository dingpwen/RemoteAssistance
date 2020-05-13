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
        user_json["number"] = user.number
        user_json["imageUrl"] = user.image
        user_array.append(user_json)
    return user_array


def friend_to_json(friends):
    friend_array = []
    for friend in friends:
        friend_json = dict()
        friend_json["name"] = friend.name
        friend_json["user_token"] = friend.token
        friend_json["number"] = friend.number
        friend_json["imageUrl"] = friend.image
        friend_json["intimacy"] = friend.intimacy
        friend_array.append(friend_json)
    return friend_array


class FriendHandler(BaseHandler, ABC):
    @check_token
    async def get(self, *args, **kwargs):
        token = self.get_query_argument("token")
        url = self.request.uri
        if url.startswith("/friend/list"):
            await self.get_friend_list(token)
            return
        elif url.startswith("/friend/sn_list"):
            sn_list = self.get_query_argument("sn_list")
            await self.get_friend_list_by_sn(token, sn_list)
            return
        elif url.startswith("/friend/helpers"):
            await self.get_helpers(token)
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
            await self.delete_friend(token, ftk)
        elif url.startswith("/friend/intimacy"):
            intimacy = self.get_body_argument('intimacy')
            await self.update_intimacy(token, ftk, intimacy)
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
            result["friends"] = friend_to_json(friends)
        print("result:", json.dumps(result))
        self.write(json.dumps(result))

    async def get_friend_list_by_sn(self, token, sn_list):
        result = dict()
        friends = FriendModel.get_friend_by_sn(token, sn_list)
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

    async def get_helpers(self, token):
        result = dict()
        friends = FriendModel.get_helpers(token)
        if friends is None:
            result["status"] = 201
            result["msg"] = "no friend"
        else:
            result["status"] = 200
            result["friends"] = use_to_json(friends)
        print("result:", json.dumps(result))
        self.write(json.dumps(result))

    async def delete_friend(self, token, ftk):
        FriendModel.del_friend(token, ftk)
        result = dict()
        result["status"] = 200
        result["msg"] = "add success"
        self.write(json.dumps(result))

    async def update_intimacy(self, token, ftk, intimacy):
        FriendModel.update_intimacy(token, ftk, intimacy)
        result = dict()
        result["status"] = 200
        result["msg"] = "add success"
        self.write(json.dumps(result))