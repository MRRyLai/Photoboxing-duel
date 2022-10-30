import socket
import sys
# 192.168.43.241 手機網路
# 192.168.0.10 家用網路

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect(('192.168.0.10', 6666))

while True:
    msg = input("Say something...\n")
    if(msg == "" or msg == " "):
        print('Input something!!!\n')
    else:
        s.send(msg.encode('utf-8'))
print("goodbye!")
