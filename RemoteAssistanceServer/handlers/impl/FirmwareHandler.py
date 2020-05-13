from abc import ABC
from handlers.base.BaseHandler import BaseHandler
from models.firmware import FirmwareModel
import json
import binascii
import gzip


class FirmwareHandler(BaseHandler, ABC):
    async def get(self, *args, **kwargs):
        url = self.request.uri
        if url.startswith("/firmware/newest"):
            await self.get_newest_version()
        elif url.startswith("/firmware/bin"):
            file_name = self.get_query_argument("filename")
            version = self.get_query_argument("version")
            await self.get_newest_bin(file_name, version)
        else:
            self.render("../../pages/firmware.html")

    async def post(self, *args, **kwargs):
        import os
        files = self.request.files
        version = self.get_argument('my_version')
        for file in files:
            up_file = files[file]
            for fileObj in up_file:
                file_path = 'static/firmware/'
                if not os.path.exists(file_path):
                    os.mkdir(file_path)
                file_path = 'static/firmware/{}'.format(fileObj.filename)
                with open(file_path, 'wb') as f:
                    f.write(fileObj.body)
                    check_sum = binascii.crc32(fileObj.body)
                    print("check_sum:", check_sum)
                    FirmwareModel.add_new_version_file(fileObj.filename, version, check_sum)
        self.write('上传成功')

    async def get_newest_version(self):
        result = dict()
        firmware = FirmwareModel.get_newest_version()
        if firmware is None:
            result["status"] = -1
            result["msg"] = "no firmware exist"
        else:
            result["status"] = 200
            result["filename"] = firmware.file_name
            result["version"] = firmware.version
            result["checksum"] = firmware.check_sum
        self.write(json.dumps(result))

    async def get_newest_bin(self, file_name, version):
        firmware = FirmwareModel.check_newest_version(file_name, version)
        if firmware is None:
            result = dict()
            result["status"] = -1
            result["msg"] = "firmware not exist"
            self.write(json.dumps(result))
        else:
            path = 'static/firmware/{}'.format(file_name)
            try:
                bin_file = open(path, "rb")
            except FileNotFoundError:
                self.write_error(404)
                return
            self.write(gzip.compress(bin_file.read()))
            self.set_header('Content-Type', 'application/octet-stream')
