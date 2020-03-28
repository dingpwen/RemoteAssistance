from abc import ABC

from tornado.websocket import WebSocketHandler, WebSocketClosedError
import json
from libs.encode import Encode

clients = dict()


class WebSocketServerHandler(WebSocketHandler, ABC):
    token = None
    pair_token = None

    def open(self, *args, **kwargs):
        self.token = self.get_arguments("token")[0]
        print("connect token:", self.token)
        if (self.token is None) or (Encode.check(self.token) is False):
            self.close(1002, "token is none or invalid")
            return
        clients[self.token] = {"token": self.token, "object": self}
        self.set_nodelay(True)
        pass

    async def on_message(self, message):
        if type(message) == str:
            try:
                json_msg = json.loads(message)
            except json.decoder.JSONDecodeError:
                print("none-json message:", message)
                return
            if json_msg is None:
                return
            command = json_msg["command"]
            goal_token = json_msg["goal_token"]
            if command is None or goal_token is None:
                print("bad message")
                return
            if command == "heart":
                print("receive heart from: ", self.token)
                return
            if command == "start_help":
                self.pair_token = goal_token
                socket_client = self.find_client(self.pair_token)
                if socket_client is not None:
                    if socket_client.pair_token is not None:
                        await self.send_invalid(socket_client.pair_token)
                        return
                    socket_client.pair_token = self.token
                    json_str = dict()
                    json_str["command"] = "start_help"
                    json_str["token"] = self.token
                    socket_client.write_message(json.dumps(json_str))
                else:
                    print("target client is closed")
                    await self.send_end(goal_token)
        else:
            print("byte data")
            socket_client = self.find_client(self.pair_token)
            if socket_client is not None:
                result = await self.send_byte_data(socket_client, message)
                if result == -1:
                    print("Image send fail")
                    self.pair_token = None
            else:
                print("target client is closed")
                await self.send_end(self.pair_token)

    def on_close(self):
        self.pair_token = None
        if self.token in clients:
            del clients[self.token]
            print("client %s is closed" % self.token)

    def check_origin(self, origin):
        return True

    async def send_end(self, token):
        json_str = dict()
        json_str["command"] = "end_help"
        json_str["token"] = token
        await self.write_message(json.dumps(json_str))
        pass

    async def send_invalid(self, token):
        json_str = dict()
        json_str["command"] = "invalid"
        json_str["token"] = token
        await self.write_message(json.dumps(json_str))
        pass

    @staticmethod
    async def send_byte_data(client, message):
        try:
            await client.write_message(message, True)
            return 0
        except WebSocketClosedError:
            return -1

    @staticmethod
    def find_client(client_token):
        if client_token is not None and client_token in clients:
            client = clients[client_token]
            return client["object"]
        return None
