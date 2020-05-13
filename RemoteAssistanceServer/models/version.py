from libs.db import Base, session
from sqlalchemy import Column, Integer, String
from sqlalchemy.exc import InvalidRequestError
import time


class Version(Base):
    __tablename__ = 'version'
    id = Column(Integer, primary_key=True)
    project = Column(String(20), nullable=False)
    version_name = Column(String(30), nullable=False)
    base_code = Column(Integer, default=1)
    version_code = Column(Integer)
    file_type = Column(Integer, default=0)
    file_name = Column(String(30))
    apk_check_sum = Column(String(30))
    patch_check_sum = Column(String(30))
    adt = Column(String(20))

    def __init__(self, project, version_name, base_code, version_code,
                 file_type, file_name, apk_check_sum, patch_check_sum, adt):
        self.project = project
        self.version_name = version_name
        self.base_code = base_code
        self.version_code = version_code
        self.file_type = file_type
        self.file_name = file_name
        self.apk_check_sum = apk_check_sum
        self.patch_check_sum = patch_check_sum
        self.adt = adt

    def _repr__(self):
        return "{'version':'%d', 'file_type':'%d', 'path':'%s', 'check_sum1':'%s', 'check_sum2':'%s'}" % (
            self.version_code, self.file_type, self.file_name, self.apk_check_sum, self.patch_check_sum)


class VersionModel:
    @staticmethod
    def get_lasted_version(project):
        return session.query(Version).filter(Version.project == project).order_by(Version.version_code.desc()).first()

    @staticmethod
    def add_new_version(project, version_name, base_code, version_code, file_type, file_name,
                        apk_check_sum, patch_check_sum):
        adt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        try:
            version = Version(project, version_name, base_code, version_code, file_type, file_name,
                              apk_check_sum, patch_check_sum, adt)
            session.add(version)
            session.commit()
        except InvalidRequestError:
            session.rollback()

    @staticmethod
    def get_patch_file(project, base, version, file_type):
        if file_type == 1:
            return session.query(Version.file_name).filter(Version.project == project, Version.version_code == version).first()
        return session.query(Version.file_name).filter(Version.project == project, Version.base_code == base,
                                                       Version.version_code == version).first()

    @staticmethod
    def get_projects():
        return session.query(Version.project).distinct().all()

