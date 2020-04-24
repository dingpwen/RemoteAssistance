from libs.db import Base, session
from sqlalchemy import Column, Integer, String
from sqlalchemy.exc import InvalidRequestError
import time

class Qrcode(Base):
    __tablename__ = 'qrcode'
    id = Column(Integer, primary_key=True)
    code = Column(String(200))
    content = Column(String(500))
    url = Column(String(100))
    image = Column(String(60))
    ut = Column(String(20))

    def __init__(self, code, content, url, image, ut):
        self.code = code
        self.content = content
        self.url = url
        self.image = image
        self.ut = ut;
        pass

class QrcodeModel:
    @staticmethod
    def do_save(code, content, url, image):
        try:
            ut = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
            qrcode = Qrcode(code, content, url, image, ut)
            session.add(qrcode)
            session.commit()
        except InvalidRequestError:
            session.rollback()

    @staticmethod
    def get_content(code):
        return session.query(Qrcode).filter(Qrcode.code == code).first()