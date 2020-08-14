from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, scoped_session

#db_url = "mysql+pymysql://root:Wen_315845@124.70.140.183:3306/third_eye?charset=utf8mb4"
db_url = "mysql+pymysql://root:Wen_315845@localhost:3306/third_eye?charset=utf8mb4"
engine = create_engine(db_url, echo=False, pool_recycle=7200)
Session = sessionmaker(bind=engine)
session = scoped_session(Session)
Base = declarative_base(engine)


def init_db():
    Base.metadata.create_all(engine)
