from libs.encode import Encode


def check_token(func):
    def wrapper(self, *args, **kwargs):
        token = self.get_arguments("token")[0]
        if Encode.check(token):
            return func(self, *args, **kwargs)
        print("invalid token:", token)
        self.write('invalid token')
        self.send_error(404)
        return
    return wrapper
