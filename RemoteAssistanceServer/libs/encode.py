import base64
import hashlib
from settings.config import user_token_fix, user_token_fix_pos
import time
import binascii


class Encode:
    @staticmethod
    def generate_token():
        ct = time.time()
        md5 = hashlib.md5(str(ct).encode(encoding='UTF-8')).digest()
        fix = hashlib.md5(user_token_fix.encode(encoding='UTF-8')).digest()
        data = md5[:user_token_fix_pos] + fix[:] + md5[user_token_fix_pos:]
        return base64.b64encode(data).decode('ascii')

    @staticmethod
    def encode_str(content):
        ct = time.time()
        if len(content) > 50:
            content = content[:50] + str(ct)
        else:
            content = content + str(ct)
        md5 = hashlib.md5(content.encode(encoding='UTF-8')).digest()
        fix = hashlib.md5(user_token_fix.encode(encoding='UTF-8')).digest()
        data = md5[:user_token_fix_pos] + fix[:] + md5[user_token_fix_pos:]
        return base64.b64encode(data).decode('ascii')

    @staticmethod
    def decode(encode_str):
        try:
            return base64.b64decode(encode_str)
        except binascii.Error:
            return None

    @staticmethod
    def check(token):
        if token is None:
            return False
        fix = hashlib.md5(user_token_fix.encode(encoding='UTF-8')).digest()
        try:
            data = base64.b64decode(token)
            if len(data) < len(fix) + user_token_fix_pos:
                return False
            if data[user_token_fix_pos:user_token_fix_pos + len(fix)] == fix:
                print("True True")
                return True
        except binascii.Error:
            return False
        '''for i in range(0, len(fix)):
            if data[i + user_token_fix_pos] != fix[i]:
                return False'''
        return False
