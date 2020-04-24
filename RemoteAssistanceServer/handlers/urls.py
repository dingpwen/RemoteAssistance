from handlers.impl.WebSocketServer import WebSocketServerHandler
from handlers.impl.IndexHandler import IndexHandler
from handlers.impl.FriendHandler import FriendHandler
from handlers.impl.UserHandler import UserHandler
from handlers.impl.ImageHandler import ImageHandler
from handlers.impl.QrcodeHandler import QrcodeHandler
from tornado.web import StaticFileHandler
import os
handlers = [
    (r'/', IndexHandler),
    (r'/socket', WebSocketServerHandler),
    (r'/friend/(.*)', FriendHandler),
    (r'/user/(.*)', UserHandler),
    (r'/image/(.*)', ImageHandler),
    (r'/qrcode/(.*)', QrcodeHandler),
    (r'/qr_image/(.*)', StaticFileHandler,
     {"path": os.path.join(os.path.dirname(__file__), "../static/qrcode"), "default_filename": "default.jpg"}),

]
