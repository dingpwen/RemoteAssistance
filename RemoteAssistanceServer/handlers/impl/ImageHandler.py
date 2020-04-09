from abc import ABC
from handlers.base.BaseHandler import BaseHandler
from libs.hooks import check_token
from libs.encode import Encode
import time
import io
import json
from PIL import Image, ImageFile
from settings.config import max_img_size

ImageFile.LOAD_TRUNCATED_IMAGES = True


class ImageHandler(BaseHandler, ABC):
    @check_token
    async def get(self, *args, **kwargs):
        url = self.request.uri
        image_url = self.get_query_argument("image")
        if url.startswith("/image/get") and image_url is not None:
            file_name = 'static/upload/{}'.format(image_url)
            print("image:", file_name)
            await self.download(file_name)
            pass
        else:
            await self.send_invalid_path()

    @check_token
    async def post(self, *args, **kwargs):
        url = self.request.uri
        token = self.get_body_argument('token')
        result = dict()
        if url.startswith("/image/upload"):
            image = self.get_body_argument('image')
            image_data = Encode.decode(image)
            if image_data is None:
                result["status"] = 404
                result["msg"] = "image data is invalid"
                self.write(json.dumps(result))
                return
            ut = int(time.time())
            file_name = token.replace("/", "_") + str(ut) + ".jpg"
            save_to = 'static/upload/{}'.format(file_name)
            await self.upload(image_data, save_to)
            result["status"] = 200
            result["imageUrl"] = file_name
            self.write(json.dumps(result))
        else:
            await self.send_invalid_path()

    async def download(self, file_name):
        try:
            pic = open(file_name, "rb")
        except FileNotFoundError:
            self.write_error(404)
            return
        self.write(pic.read())
        self.set_header('Content-Type', 'image/png')
        pass

    '''async def download(self, file_name):
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
        self.set_header('Content-Type', 'image/png')
        self.write(bio.getvalue())
        pass'''

    @staticmethod
    async def upload(image_data, save_to):
        with open(save_to, 'wb') as f:
            f.write(image_data)
        f.close()
        img = Image.open(save_to)
        height = img.size[1]
        width = img.size[0]
        while (height > max_img_size) or (width > max_img_size):
            height = height // 2
            width = width // 2
        img = img.resize((width, height))
        img.save(save_to)
