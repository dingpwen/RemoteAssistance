from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker

db_url = "mysql+pymysql://localhost:3306@root:315845/viwalk?charset=utf8"
engine = create_engine(db_url, echo=False)
Session = sessionmaker(bind=engine)
session = Session()
Base = declarative_base(engine)
