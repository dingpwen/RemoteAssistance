from libs.db import Base, session
from sqlalchemy import Column, Integer, String
from sqlalchemy.exc import InvalidRequestError
import time


class Firmware(Base):
    __tablename__ = 'firmware'
    id = Column(Integer, primary_key=True)
    file_name = Column(String(20), nullable=False)
    version = Column(String(20), nullable=False)
    check_sum = Column(String(40), nullable=False)
    adt = Column(String(20))

    def __init__(self, file_name, version, check_sum, adt):
        self.file_name = file_name
        self.version = version
        self.check_sum = check_sum
        self.adt = adt

    def _repr__(self):
        return "{'file':'%s', 'version':'%s', 'checkSum':'%d', 'time':'%s'}" % (
            self.file_name, self.version, self.check_sum, self.adt)


class FirmwareModel:
    @staticmethod
    def add_new_version_file(file_name, version, check_sum):
        adt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        try:
            firmware = Firmware(file_name, version, check_sum, adt)
            session.add(firmware)
            session.commit()
        except InvalidRequestError:
            session.rollback()

    @staticmethod
    def get_newest_version():
        return session.query(Firmware).order_by(Firmware.adt.desc()).first()

    @staticmethod
    def check_newest_version(file_name, version):
        return session.query(Firmware).filter(Firmware.file_name == file_name, Firmware.version == version).first()

