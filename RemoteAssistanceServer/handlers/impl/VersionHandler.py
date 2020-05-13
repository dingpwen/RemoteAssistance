from abc import ABC
from handlers.base.BaseHandler import BaseHandler
from models.version import VersionModel
import json
import binascii


class VersionHandler(BaseHandler, ABC):
    async def get(self, *args, **kwargs):
        url = self.request.uri
        project = self.get_query_argument("project")
        if url.startswith("/version/newest"):
            await self.get_newest_version(project)
        elif url.startswith("/version/patch"):
            base = self.get_query_argument("base")
            version = self.get_query_argument("version")
            file_type = self.get_query_argument("type")
            await self.get_patch(project, base, version, file_type)
        elif url.startswith("/version/load"):
            if project == "none":
                await self.on_page_load()
            else:
                await self.on_project_change(project)
        else:
            self.render("../../pages/version.html")

    async def post(self, *args, **kwargs):
        import os
        files = self.request.files
        base = self.get_argument('base_version')
        version = self.get_argument('cur_version')
        name = self.get_argument('version_name')
        project = self.get_argument('project')
        if project == "Other":
            project = self.get_argument('project2')
        file_type = self.get_argument('file_type')
        checksum = self.get_argument('checksum')
        print(project, file_type, base, version, name, checksum)
        for file in files:
            up_file = files[file]
            for fileObj in up_file:
                file_path = 'static/version/{}'.format(project)
                if not os.path.exists(file_path):
                    os.makedirs(file_path)
                file_path = 'static/version/{}/{}'.format(project, fileObj.filename)
                with open(file_path, 'wb') as f:
                    f.write(fileObj.body)
                    check_sum = binascii.crc32(fileObj.body)
                    print("check_sum:", check_sum)
                    if file_type == 0:
                        VersionModel.add_new_version(project, name, base, version, file_type, fileObj.filename,
                                                     checksum, check_sum)
                    else:
                        VersionModel.add_new_version(project, name, base, version, file_type, fileObj.filename,
                                                     check_sum, "0")
        self.write('上传成功')

    async def get_newest_version(self, project):
        result = dict()
        version = VersionModel.get_lasted_version(project)
        if version is None:
            result["status"] = -1
            result["msg"] = "no apk/patch exist"
        else:
            result["status"] = 200
            result["version"] = version.version_code
            result["name"] = version.version_name
            result["type"] = version.file_type
            result["checksum1"] = version.apk_check_sum
            result["checksum2"] = version.patch_check_sum
        self.write(json.dumps(result))

    async def get_patch(self, project, base, version, file_type):
        file_name = VersionModel.get_patch_file(project, base, version, file_type)
        print(file_name)
        if file_name is None:
            result = dict()
            result["status"] = -1
            result["msg"] = "patch not exist"
            self.write(json.dumps(result))
        else:
            path = 'static/version/{}/{}'.format(project, file_name[0])
            print(path)
            try:
                bin_file = open(path, "rb")
            except FileNotFoundError:
                self.write_error(404)
                return
            self.write(bin_file.read())
            self.set_header('Content-Type', 'application/octet-stream')

    async def on_page_load(self):
        projects = VersionModel.get_projects()
        result = dict()
        if len(projects) > 0:
            project = projects[0][0]
            version = VersionModel.get_lasted_version(project)
            result["version"] = version.version_code
            project_set = ""
            for project in projects:
                project_set = project_set + project[0] + ","
            project_str = project_set[:len(project_set) -1]
            result["projects"] = project_str
        else:
            result["projects"] = "none"
        self.write(json.dumps(result))

    async def on_project_change(self, project):
        result = dict()
        version = VersionModel.get_lasted_version(project)
        result["version"] = version.version_code
        self.write(json.dumps(result))
