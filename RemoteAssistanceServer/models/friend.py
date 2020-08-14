from libs.db import Base, session
from sqlalchemy import Column, Integer, String, func
from sqlalchemy.exc import InvalidRequestError
from models.user import User
from settings.config import max_helpers
import time


class Friend(Base):
    __tablename__ = 'friends'
    id = Column(Integer, primary_key=True)
    token = Column(String(100))
    ftk = Column(String(100))
    category = Column(Integer)
    intimacy = Column(Integer, default=0)
    adt = Column(String(20))

    def __init__(self, token, ftk, category, intimacy, adt):
        self.category = category
        self.token = token
        self.ftk = ftk
        self.intimacy = intimacy
        self.adt = adt

    def __repr__(self):
        return "{'token':'%s', 'ftk':'%s', 'category':'%d', 'time':'%s'}" % (
            self.token, self.ftk, self.category, self.adt)


class FriendModel:
    @staticmethod
    def get_friends(token):
        '''user_set1 = session.query(User).join(Friend, Friend.ftk == User.token).filter(Friend.token == token).all()
        user_set2 = session.query(User).join(Friend, Friend.token == User.token).filter(Friend.ftk == token).all()
        return user_set1 + user_set2'''
        return session.query(User.name, User.token, User.number, User.image, Friend.intimacy)\
            .join(Friend, Friend.ftk == User.token)\
            .filter(Friend.token == token).order_by(Friend.intimacy.desc()).all()

    @staticmethod
    def get_friend_by_sn(token, sn_list):
        t_list = tuple([str(i) for i in sn_list.split(',')])
        return session.query(User).join(Friend, Friend.ftk == User.token)\
            .filter(Friend.token == token, User.sn.in_(t_list)).order_by(Friend.intimacy.desc()).all()

    @staticmethod
    def del_friend(token, ftk):
        try:
            session.query(Friend).filter(Friend.ftk == ftk, Friend.token == token).delete()
            session.query(Friend).filter(Friend.token == ftk, Friend.ftk == token).delete()
            session.commit()
        except InvalidRequestError:
            session.rollback()
            pass

    @staticmethod
    def add_friend(token, ftk):
        friend = session.query(Friend).filter(Friend.ftk == ftk, Friend.token == token).first()
        if friend is not None:
            return -1, friend
        my_category = session.query(User).filter(User.token == token).first().category
        if my_category == 2:
            count = session.query(func.count(Friend.id)).filter(Friend.token == token).scalar()
            if count > max_helpers:
                return -2, None
        user = session.query(User).filter(User.token == ftk).first()
        if user is None:
            return -3, None
        friend_category = user.category
        if friend_category == 2:
            count = session.query(func.count(Friend.id)).filter(Friend.ftk == ftk, Friend.category == 2).scalar()
            if count > max_helpers:
                return -2, None
        if my_category == 2 and friend_category == 2:
            return -4, None
        intimacy = 0
        if my_category == 1 and friend_category == 2:
            intimacy = 1
        adt = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
        friend = Friend(token, ftk, friend_category, intimacy, adt)
        session.add(friend)
        friend = Friend(ftk, token, my_category, 0, adt)
        session.add(friend)
        session.commit()
        friend = session.query(Friend).filter(Friend.ftk == ftk, Friend.token == token).first()
        return 0, friend

    @staticmethod
    async def update_token(old_token, new_token):
        try:
            session.query(Friend).filter(Friend.ftk == old_token).update({Friend.ftk: new_token})
            session.query(Friend).filter(Friend.token == old_token).update({Friend.token: new_token})
            session.commit()
        except InvalidRequestError:
            session.rollback()

    @staticmethod
    def get_helpers(token):
        return session.query(User).join(Friend, Friend.ftk == User.token)\
            .filter(Friend.token == token, User.category == 2, User.number != "").all()

    @staticmethod
    def update_intimacy(token, ftk, intimacy):
        try:
            session.query(Friend).filter(Friend.ftk == ftk, Friend.token == token).update({Friend.intimacy: intimacy})
            session.commit()
        except InvalidRequestError:
            session.rollback()
