from handlers.impl.WebSocketServer import WebSocketServerHandler
from handlers.impl.IndexHandler import IndexHandler
from handlers.impl.FriendHandler import FriendHandler
from handlers.impl.UserHandler import UserHandler
from handlers.impl.ImageHandler import ImageHandler
handlers = [
    (r'/', IndexHandler),
    (r'/socket', WebSocketServerHandler),
    (r'/friend/(.*)', FriendHandler),
    (r'/user/(.*)', UserHandler),
    (r'/image/(.*)', ImageHandler),
]
