from abc import ABC
from tornado.web import RequestHandler
from models.user import UserModel
from libs.hooks import check_token
from libs.encode import Encode
import json


class UserHandler(RequestHandler, ABC):
    async def get(self, *args, **kwargs):
        pass

    @check_token
    async def post(self):
        url = self.request.uri
        token = self.get_body_argument('token')
        category = self.get_body_argument('category')
        result = dict()
        if url.startswith("/user/add"):
            res, user = await UserModel.add_user(token, category)
            if res == 0:
                result["status"] = 200
                result["msg"] = "add success"
            else:
                result["status"] = res
                result["msg"] = "add Fail"
            return json.dumps(result)
        elif url.startswith("/user/register"):
            name = self.get_body_argument('name')
            number = self.get_body_argument('number')
            image = self.get_body_argument('image')
            res, user = await UserModel.add_user(token,  name, number, image, category)
            if res == 0:
                result["status"] = 200
                result["msg"] = "add success"
            else:
                result["status"] = res
                result["msg"] = "add Fail"
            return json.dumps(result)
        else:
            pass
        self.write('invalid path')
        self.send_error(404)
