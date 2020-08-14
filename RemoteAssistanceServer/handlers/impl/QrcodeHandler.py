from abc import ABC
from handlers.base.BaseHandler import BaseHandler
from libs.encode import Encode
from models.qrcode import QrcodeModel
import qrcode
import time
import json
from settings.config import server_ip


class QrcodeHandler(BaseHandler, ABC):
    async def get(self, *args, **kwargs):
        url = self.request.uri
        if url.startswith("/qrcode/content"):
            code = self.get_query_argument('code')
            await self.get_content(code)
        elif url.startswith("/qrcode/code="):
            start = url.find("code=")
            end = url.find("+edoc")
            if start > 0 and end > 0:
                code = url[start + 5:end]
                print("code=", code)
                await self.get_code(code)
            else:
                await self.send_invalid_path()
        else:
            self.render("../../pages/generate.html")

    async def post(self, *args, **kwargs):
        content = self.get_argument("content", None)
        url = self.get_argument("url", None)
        await self.generate_and_save(content, url)

    async def generate_and_save(self, content, url):
        code = Encode.encode_str(content)
        print("code:", code)
        qr_code = server_ip + "qrcode/code={}+edoc".format(code)
        img = qrcode.make(data=qr_code)
        ut = int(time.time())
        file_name = code.replace("/", "_")[:16] + str(ut) + ".jpg"
        save_to = 'static/qrcode/{}'.format(file_name)
        img.save(save_to)
        QrcodeModel.do_save(code, content, url, file_name)
        result = dict()
        html_path = '../qr_image/{}'.format(file_name)
        result["status"] = 200
        result["qr_code"] = qr_code
        result["image"] = html_path
        self.write(json.dumps(result))

    async def get_content(self, code):
        qr_code = QrcodeModel.get_content(code)
        print(code)
        print(qr_code)
        result = dict()
        if qr_code is None:
            result["status"] = -1
            result["msg"] = "error qrcode"
        else:
            result["status"] = 200
            result["content"] = qr_code.content
        self.write(json.dumps(result))

    async def get_code(self, code):
        qr_code = QrcodeModel.get_content(code)
        self.write(qr_code.content)
