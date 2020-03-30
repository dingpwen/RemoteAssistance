import tornado
from tornado import httpserver, ioloop
from tornado.options import define, options
from handlers.urls import handlers
from settings.config import settings
from libs.db import init_db
'''from libs.encode import Encode'''

define("port", default=5000, help="server port", type=int)
define("start", default=True, help="run server", type=bool)
init_db()

if __name__ == "__main__":
    options.parse_command_line()
    print("options.start:", options.start)
    print("options.port:", options.port)
    '''token = Encode.generate_token()
    Encode.check(token)'''
    if options.start:
        app = tornado.web.Application(handlers, settings)
        http_server = httpserver.HTTPServer(app)
        http_server.listen(options.port)
        print("start server on port:", options.port)
        ioloop.IOLoop.instance().start()
