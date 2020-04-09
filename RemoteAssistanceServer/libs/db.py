from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

db_url = "mysql+pymysql://root:Wen_315845@121.36.10.45:3306/third_eye?charset=utf8mb4"
engine = create_engine(db_url, echo=False)
Session = sessionmaker(bind=engine)
session = Session()
Base = declarative_base(engine)


def init_db():
    Base.metadata.create_all(engine)
