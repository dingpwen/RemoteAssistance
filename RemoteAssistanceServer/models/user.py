from libs.db import Base, session
from sqlalchemy import Sequence
from sqlalchemy import Column, Integer, String
from sqlalchemy.exc import InvalidRequestError
from libs.encode import Encode
import time


class User(Base):
    __tablename__ = "users"
    id = Column(Integer, Sequence('user_id_seq'), primary_key=True)
    name = Column(String(50))
    number = Column(String(20))
    password = Column(String(50))
    token = Column(String(100))
    image = Column(String(100))
    # 1:visual impaired 2: helper
    category = Column(Integer)
    sn = Column(String(30))
    rt = Column(String(20))
    lt = Column(String(20))

    def __init__(self, name, number, password, token, image, category, sn, rt, lt):
        self.name = name
        self.number = number
        self.password = password
        self.token = token
        self.image = image
        self.category = category
        self.sn = sn
        self.rt = rt
        self.lt = lt
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
        user = User("", "", "", token, "", category, "", rt, rt)
        session.add(user)
        session.commit()
        user = session.query(User).filter(User.token == token).first()
        return 0, user

    @staticmethod
    def reg_user(number, password, category, dev_sn):
        user = session.query(User).filter(User.number == number).first()
        if user is not None:
            return -1, user
        rt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        token = Encode.generate_token()
        user = User("", number, password, token, "", category, dev_sn, rt, rt)
        session.add(user)
        session.commit()
        user = session.query(User).filter(User.token == token).first()
        return 0, user

    @staticmethod
    def get_user(token):
        return session.query(User).filter(User.token == token).first()

    @staticmethod
    def get_reg_user(number, password):
        user = session.query(User).filter(User.number == number, User.password == password).first()
        otk = ""
        if user is not None:
            otk = user.token
            user.token = Encode.generate_token()
            user.lt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
            session.query(User).filter(User.number == number).update({User.token: user.token, User.lt: user.lt})
            session.commit()
        return otk, user

    @staticmethod
    def update_user(token, name, image):
        user = None
        try:
            session.query(User).filter(User.token == token).update({User.name: name, User.image: image})
            session.commit()
            user = session.query(User).filter(User.token == token).first()
        except InvalidRequestError:
            session.rollback()
        except Exception as e:
            print(e)
        return user

    @staticmethod
    def update_sn(token, sn):
        try:
            session.query(User).filter(User.token == token).update({User.sn: sn})
            session.commit()
        except InvalidRequestError:
            session.rollback()
        except Exception as e:
            print(e)
