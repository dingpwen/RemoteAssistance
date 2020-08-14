from abc import ABC
from handlers.base.BaseHandler import BaseHandler
from models.user import UserModel
from models.friend import FriendModel
from libs.hooks import check_token
import json


def user_to_json(user):
    user_json = dict()
    if user is not None:
        user_json["name"] = user.name
        user_json["number"] = user.number
        user_json["user_token"] = user.token
        user_json["imageUrl"] = user.image
        user_json["sn"] = user.sn
    return user_json


class UserHandler(BaseHandler, ABC):
    @check_token
    async def get(self, *args, **kwargs):
        url = self.request.uri
        if url.startswith("/user/login"):
            number = self.get_query_argument('number')
            password = self.get_query_argument('password')
            await self.login(number, password)
        elif url.startswith("/user/info"):
            token = self.get_query_argument('token')
            await self.get_info(token)
        else:
            await self.send_invalid_path()

    @check_token
    async def post(self, *args, **kwargs):
        url = self.request.uri
        token = self.get_body_argument('token')
        if url.startswith("/user/add"):
            category = self.get_body_argument('category')
            await self.add_user(token, category)
        elif url.startswith("/user/register"):
            category = self.get_body_argument('category')
            number = self.get_body_argument('number')
            password = self.get_body_argument('password')
            dev_sn = self.get_body_argument('sn')
            await self.register_user(token, number, password, category, dev_sn)
        elif url.startswith("/user/update"):
            name = self.get_body_argument('name')
            image = self.get_body_argument('image')
            await self.update_user(token, name, image)
        elif url.startswith("/user/sn_update"):
            sn = self.get_body_argument('sn')
            await self.update_sn(token, sn)
        else:
            await self.send_invalid_path()

    async def get_info(self, token):
        result = dict()
        user = UserModel.get_user(token)
        if user is None:
            result["status"] = -1
            result["msg"] = "number or password is wrong"
        else:
            result["status"] = 200
            result["user"] = user_to_json(user)
        self.write(json.dumps(result))

    async def login(self, number, password):
        result = dict()
        otk, user = UserModel.get_reg_user(number, password)
        if user is None:
            result["status"] = -1
            result["msg"] = "number or password is wrong"
        else:
            await FriendModel.update_token(otk, user.token)
            result["status"] = 200
            result["user"] = user_to_json(user)
        self.write(json.dumps(result))

    async def add_user(self, token, category):
        res, user = UserModel.add_user(token, category)
        result = dict()
        if res == 0:
            result["status"] = 200
            result["msg"] = "add success"
        else:
            result["status"] = res
            result["msg"] = "add Fail"
        self.write(json.dumps(result))

    async def register_user(self, token, number, password, category, dev_sn):
        res, user = UserModel.reg_user(number, password, category, dev_sn)
        result = dict()
        if res == 0:
            await FriendModel.update_token(token, user.token)
            result["status"] = 200
            result["user"] = user_to_json(user)
        else:
            result["status"] = res
            result["msg"] = "add Fail"
        self.write(json.dumps(result))

    async def update_user(self, token, name, image):
        user = UserModel.update_user(token, name, image)
        result = dict()
        result["status"] = 200
        if user is not None:
            result["user"] = user_to_json(user)
        self.write(json.dumps(result))

    async def update_sn(self, token, sn):
        UserModel.update_sn(token, sn)
        result = dict()
        result["status"] = 200
        result["msg"] = "update success"
        self.write(json.dumps(result))
