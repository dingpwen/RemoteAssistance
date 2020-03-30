from libs.db import Base, session
from sqlalchemy import Column, Integer, String, func
from models.user import User
from settings.config import max_helpers
import time


class Friend(Base):
    __tablename__ = 'friends'
    id = Column(Integer, primary_key=True)
    token = Column(String(100))
    ftk = Column(String(100))
    category = Column(Integer)
    adt = Column(String(20))

    def __init__(self, token, ftk, category, adt):
        self.category = category
        self.token = token
        self.ftk = ftk
        self.adt = adt

    def __repr__(self):
        return "{'token':'%s', 'ftk':'%s', 'category':'%d', 'time':'%s'}" % (
            self.token, self.ftk, self.category, self.time)


class FriendModel:
    @staticmethod
    def get_friends(token):
        return session.query(User).join(Friend, Friend.ftk == User.token).filter(Friend.token == token).all()

    @staticmethod
    def del_friend(token, ftk):
        session.query(Friend).filter(Friend.ftk == ftk, Friend.token == token).delete()
        session.commit()

    @staticmethod
    def add_friend(token, ftk):
        friend = session.query(Friend).filter(Friend.ftk == ftk, Friend.token == token).first()
        if friend is not None:
            return -1, friend
        category = session.query(User).filter(User.token == token).first().category
        if category == 2:
            count = session.query(func.count(Friend.id)).filter(Friend.token == token).scalar()
            if count > max_helpers:
                return -2, None
        user = session.query(User).filter(User.token == ftk).first()
        if user is None:
            return -3, None
        category = user.category
        if category == 2:
            count = session.query(func.count(Friend.id)).filter(Friend.ftk == ftk, Friend.category == 2).scalar()
            if count > max_helpers:
                return -2, None
        adt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        friend = Friend(token, ftk, category, adt)
        session.add(friend)
        session.commit()
        friend = session.query(Friend).filter(Friend.ftk == ftk, Friend.token == token).first()
        return 0, friend

    @staticmethod
    async def update_token(old_token, new_token):
        session.query(Friend).filter(Friend.ftk == old_token).update({Friend.ftk: new_token})
