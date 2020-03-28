from libs.db import Base, session
from sqlalchemy import Sequence
from sqlalchemy import Column, Integer, String
import time


class User(Base):
    __tablename__ = "users"
    id = Column(Integer, Sequence('user_id_seq'), primary_key=True)
    name = Column(String(50))
    number = Column(String(20))
    token = Column(String(100))
    image = Column(String(50))
    # 1:visual impaired 2: helper
    category = Column(Integer)
    rt = Column(String(20))

    def __init__(self, name, number, token, image, category, rt):
        self.name = name
        self.number = number
        self.token = token
        self.image = image
        self.category = category
        self.rt = rt
        pass

    def __repr__(self):
        return "{'name':'%s', 'number':'%s', 'token':'%s', 'category':'%d', 'time':'%s'}" % (
            self.name, self.number, self.token, self.category, self.rt)


class UserModel:
    @staticmethod
    def add_user(token, category):
        user = session.query(User).filter(User.token == token).first()
        if user is not None:
            return -1, user
        rt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        user = User("", "", token, "", category, rt)
        session.add(user)
        session.commit()
        user = session.query(User).filter(User.token == token).first()
        return 0, user

    @staticmethod
    def add_user(ltk, name, number, image, category):
        user = session.query(User).filter(User.number == number).first()
        if user is not None:
            return -1, user
        rt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        token = ""
        user = User(name, number, token, image, category, rt)
        session.add(user)
        session.commit()
        user = session.query(User).filter(User.token == token).first()
        return 0, user

    @staticmethod
    def get_user(token):
        return session.query(User).filter(User.token == token).first()
