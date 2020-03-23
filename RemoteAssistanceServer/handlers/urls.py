from handlers.impl.WebSocketServer import WebSocketServerHandler
from handlers.impl.IndexHandler import IndexHandler
from handlers.impl.FriendHandler import FriendHandler
handlers = [
    (r'/', IndexHandler),
    (r'/socket', WebSocketServerHandler),
    (r'/friend/(.*)', FriendHandler),
]
