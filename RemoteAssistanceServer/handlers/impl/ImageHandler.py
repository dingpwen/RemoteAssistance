from abc import ABC
from tornado.web import RequestHandler
from libs.hooks import check_token
from libs.encode import Encode
import time
import io
import json
from PIL import Image
from settings.config import max_img_size


class ImageHandler(RequestHandler, ABC):
    async def get(self, *args, **kwargs):
        url = self.request.uri
        if url.startswith("/image/") and url.endswith(".jpg"):
            file_name = 'static/upload/{}'.format(url[7:])
            await self.download(file_name)
            pass
        else:
            self.write("Invalid path ")
            self.write_error(404)

    @check_token
    async def post(self):
        url = self.request.uri
        token = self.get_body_argument('token')
        if url.startswith("/image/upload"):
            image = self.get_body_argument('image')
            image_data = Encode.decode(image)
            ut = int(time.time())
            file_name = token + str(ut) + ".jpg"
            save_to = 'static/upload/{}'.format(file_name)
            await self.upload(image_data, save_to)
            result = dict()
            result["status"] = 200
            result["imageUrl"] = file_name
            self.write(json.dumps(result))
        else:
            self.write_error(404)

    async def download(self, file_name):
        try:
            img = Image.open(file_name)
            height = img.size[1]
            width = img.size[0]
            while (height > max_img_size) or (width > max_img_size):
                height = height//2
                width = width//2
                print(width, height)
            img = img.resize((width, height))
        except FileNotFoundError:
            self.write_error(404)
            return
        bio = io.BytesIO()
        img.save(bio, "PNG")
        self.set_header('Content-Type', 'image/PNG')
        self.write(bio.getvalue())
        pass

    @staticmethod
    async def upload(image_data, save_to):
        with open(save_to, 'wb') as f:
            f.write(image_data)
