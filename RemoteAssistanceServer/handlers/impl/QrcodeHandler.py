from abc import ABC
from handlers.base.BaseHandler import BaseHandler
from libs.encode import Encode
from models.qrcode import QrcodeModel
import qrcode
import time
import json


class QrcodeHandler(BaseHandler, ABC):
    async def get(self, *args, **kwargs):
        url = self.request.uri
        if url.startswith("/qrcode/content"):
            code = self.get_query_argument('code')
            await self.get_content(code)
        else:
            self.render("../../pages/generate.html")

    async def post(self, *args, **kwargs):
        content = self.get_argument("content", None)
        url = self.get_argument("url", None)
        await self.generate_and_save(content, url)

    async def generate_and_save(self, content, url):
        code = Encode.encode_str(content)
        print("code:", code)
        img = qrcode.make(data=code)
        ut = int(time.time())
        file_name = code.replace("/", "_")[:16] + str(ut) + ".jpg"
        save_to = 'static/qrcode/{}'.format(file_name)
        img.save(save_to)
        QrcodeModel.do_save(code, content, url, file_name)
        result = dict()
        html_path = '../qr_image/{}'.format(file_name)
        result["status"] = 200
        result["image"] = html_path
        self.write(json.dumps(result))

    async def get_content(self, code):
        qrcode = QrcodeModel.get_content(code)
        print(code)
        print(qrcode)
        result = dict()
        if qrcode is None:
            result["status"] = -1
            result["msg"] = "error qrcode"
        else:
            result["status"] = 200
            result["content"] = qrcode.content
        self.write(json.dumps(result))