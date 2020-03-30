from abc import ABC
from handlers.base.BaseHandler import BaseHandler
from models.user import UserModel
from models.friend import FriendModel
from libs.hooks import check_token
import json


class UserHandler(BaseHandler, ABC):
    @check_token
    async def get(self, *args, **kwargs):
        url = self.request.uri
        result = dict()
        if url.startswith("/user/login"):
            number = self.get_query_argument('number')
            password = self.get_query_argument('password')
            user = await UserModel.get_reg_user(number, password)
            if user is None:
                result["status"] = -1
                result["msg"] = "number or password is wrong"
            else:
                result["status"] = 200
                result["user"] = user
            self.write(json.dumps(result))
        else:
            await self.send_invalid_path()

    @check_token
    async def post(self, *args, **kwargs):
        url = self.request.uri
        token = self.get_body_argument('token')
        category = self.get_body_argument('category')
        print("token:", token)
        print("category:", category)
        if url.startswith("/user/add"):
            await self.add_user(token, category)
        elif url.startswith("/user/register"):
            name = self.get_body_argument('name')
            number = self.get_body_argument('number')
            password = self.get_body_argument('password')
            image = self.get_body_argument('image')
            await self.register_user(token, name, number, password, image, category)
        else:
            await self.send_invalid_path()

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

    async def register_user(self, token, name, number, password, image, category):
        res, user = UserModel.reg_user(name, number, password, image, category)
        result = dict()
        if res == 0:
            await FriendModel.update_token(token, user.token)
            result["status"] = 200
            result["msg"] = "add success"
        else:
            result["status"] = res
            result["msg"] = "add Fail"
        self.write(json.dumps(result))
