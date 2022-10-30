import socket
import binascii
import threading
import sys
import time
from queue import Queue
##################################################################資料庫區域
import mysql.connector
# import connect_database
import Database_rank
from datetime import datetime, timedelta
import pandas as pd

#完成了建立資料表以及手動插入資料



###################################################################資料庫區域
q = Queue(maxsize=10)
backlog = 10
BUF_SIZE = 1024         # Receive buffer size


HOST = '127.0.0.1'
PORT = 6666

stack = []
userid = []
conn_pool = []
addr_list = []

sleeptime = 3

def extract_ip():
    st = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:       
        st.connect(('10.255.255.255', 1))
        
        HOST = st.getsockname()[0]
    except Exception:
        HOST = '127.0.0.1'
    finally:
        st.close()
    return HOST
extract_ip()

class ServerThread(threading.Thread):
    def __init__(self, t_name, client_sc, rip, rport):
        super().__init__(name = t_name)
        self.client = client_sc
        self.rip = rip
        self.value=0
        self.username = "username"
        self.rport = rport
        self.start()            # Start the thread when it is created
    # end for __init__()
    
    def run(self):
        name = threading.current_thread().name
        time_limit = 0
        try:
            self.client.send("GetConnection.\n".encode('utf-8'))
            while True:
                self.data = self.client.recv(BUF_SIZE).decode('utf-8')
                
                # self.client_msg = self.client.recv(BUF_SIZE)
                # self.data = self.client_msg.decode('utf-8')
                #將client傳過來的string轉成int
                #print("client_msg:%s\n"%self.client_msg.decode('utf-8'))
                
                # try:
                    # conn_pool[int(index)].send(msg.encode(encoding='utf8'))
                # except ConnectionResetError:
                    # conn_pool[int(index)].close()
                    # conn_pool.remove(conn_pool[int(index)])
                    # addr_list.remove(addr_list[int(index)])     
                if(self.data=='' or self.data==' '):
                    time.sleep(sleeptime)
                    time_limit+=5
                    print(self.data)
                    print('Waiting for message from client...')
                    # self.data = self.client.recv(BUF_SIZE).decode('utf-8')
                    # print('Send msg to Client:%s'%self.data)
                    
                else:
                    #self.data = self.client_msg.decode('utf-8')
                    count=1
                    time_limit = 0
                    # print("length(self.data):%d message:%s\n"%(len(self.data),self.data))
                    self.username = self.data[:self.data.index("\n")]
                    self.value = int(self.data[self.data.index("\n")+1:])
                    
                    ###############################################################將數據傳到資料庫rank裡
                    
                    print(self.username)
                    print(self.value)
                    nowtime = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                    
                    Database_rank.insert_rank(self.username, self.value, nowtime)
                    rank_list = Database_rank.print_rank()
                    rank_df = pd.DataFrame(rank_list, columns = ['Name', 'Score', 'Updatetime'])
                    print(rank_df)
                    ###############################################################
                    
                    # self.username = self.data[0:11]
                    # self.value = int(self.data[12:len(self.data)])
                    # print("value:%d"%self.value)
                    ###############################################################
                    # for j in range (len(self.data)-1,11,-1):
                        # #print("j:%d\n"%j)
                        # self.value = self.value + int(self.data[j])*count
                        # count*=10
                    ###############################################################
                    
                    #print(type(self.value))
                    #print("self.value:%d\n"%self.value)
                    #print("self.data:%s Length:%d Last byte:%d\n"%(self.data,len(self.data),int(self.data[len(self.data)-1])))
                    #判斷int值是否為0，int值>0則int值-1，否則關閉server
                    
                    ######################################
                    # self.data = self.data+"\n"
                    # print(name)
                    # print('Get message:%s\n' %self.data)
                    ######################################
                    
                    # print('value:%d\n'%self.value)
                    
                    #########################################################################################################
                    # for item in addr_list:
                        # print('item_port:%s self.rport:%s addr_list:%s'%(item[1],self.rport,addr_list))
                        # if(item[1]==self.rport):
                            # if(len(stack)>=10):
                                # print('stack is full')
                                # pass
                            # else:
                                # stack.append(self.value)
                                # print(str(self.rport) + ' put ' + str(self.value) + ', qsize: ' + str(len(stack)))
                                # item=[]
                        # else:
                            # if(len(stack)<=0):
                                # print('queue is empty')
                                # pass
                            # else:
                                # self.data=str(stack.pop())
                                # print(str(self.rport) + ' get ' + self.data + ', qsize: ' + str(len(stack)))
                                # self.data = self.data+"\n"
                                # self.client.send(self.data.encode('utf-8'))
                                # print('Send msg to Client:%s'%self.data)
                        # print('Stack:%s'%stack)
                        # time.sleep(5)
                    #########################################################################################################
                    
                    # print('userid[0]:%s'%(userid[0]))
                    # print('conn_pool:')
                    # print(conn_pool[0])
                    # print(addr_list[0]+addr_list[1])
                    # print('item')
                    # print(item[0])
                    # if(item != []):
                        # t = ServerThread(userid[0], conn_pool[0], item[0], item[1])
                    #########################################################
                    if(len(self.data) > 0):
                    # 將int轉換成string傳送回去
                    # print((self.rip, self.rport))
                        self.data = self.data
                        print(name)
                        print('Get message:%s\n' %self.data)
                        print('value:%d\n'%self.value)
                        cur_thread = threading.current_thread()
                        stack.append((self.rport,self.value))
                        stack.reverse()
                        print('Port:%d put %d in stack'%(self.rport,self.value))
                        if(len(stack)>0):
                            i=0
                            for i in stack:
                                # print('i:')
                                # print(i)
                                # 05/18目前的功能是能夠將資料互傳但是要再多一步動作傳輸 舉例:A傳入B傳入會立即收到A資料，但是A要再傳入一次才能收到B資料(已解決)
                                if(i[0]==self.rport):
                                    pass
                                else:
                                    print('i[0]:%s i[1]:%s'%(i[0],i[1]))
                                    self.data = i[1]
                                    stack.pop([i])
                                    self.data = str(self.data)+"\n"
                                    print(self.data)
                                    self.client.send(self.data.encode('utf-8'))
                                    print('Send msg to Client.')
                                    print("Now in list:"%stack)
                                    print('Waiting for message from client...')
                                    break
                    else:
                        # print(type(self.value))
                        print('\nThe value is %d\n' %self.value)
                        print('Closing connection.\n')
                        # Close the TCP socket
                        break
                    #########################################################
                    #做成loop
                if(time_limit>=15):
                    print("Time is out!")
                    break
        except KeyboardInterrupt:
            print ('KeyboardInterrupt exception is caught')
        self.client.close()
        print(name, 'Thread closed')
        # end run()
        
    # def login_register():
        
# end for ServerThread

def main():
    # Create a TCP Server socket
    # nowtime = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    # print(nowtime)
    # rank_list = Database_rank.print_rank()
    # rank_df = pd.DataFrame(rank_list, columns = ['Name', 'Score', 'Updatetime'])
    # print(rank_df)
    srvSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    hostname = socket.gethostname()
    ip_address = socket.gethostbyname(hostname)
    # print(socket.gethostbyname(socket.gethostname()))
    print(f"Hostname: {hostname}")
    print(f"IP Address: {ip_address}")
    # Enable reuse address/port
    #srvSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    # Bind  on any incoming interface with PORT, '' is any interface
    try:
        srvSocket.bind((ip_address, PORT))
    
    except socket.error as e:
        print('Server error: %s' % str(e))
        sys.exit
    # Listen incomming connection, connection number = backlog (5)
    srvSocket.listen(backlog)
    
    # Accept the incomming connection
    #print('Waiting to receive message from client')
    
    
    # Receive client message, buffer size = BUF_SIZE
    i=0
    try:
        print("Socket Bind success!\n");
        print('Starting up server on port: %s' % (PORT))
        while True:
            i+=1
            t_name = 'Client_NO.' + str(i)
            print('Now working threads: %d' % threading.active_count())
            print('Waiting to receive message from client')
            client, (rip, rport) = srvSocket.accept()
            #print(client)
            conn_pool.append(client)
            addr_list.append((rip, rport))
            userid.append(t_name)
            print('Got connection. Create thread: %s' % t_name)
            t = ServerThread(t_name, client, rip, rport)
            # print(len(addr_list))
            for item in addr_list:
                print(item[1])
    except KeyboardInterrupt:
        print ('KeyboardInterrupt exception is caught')
    except socket.error as e:
        print('Server error: %s' % str(e))
    except Exception as e:
        print('Other exception: %s' % str(e))
    finally:
        print('Closing connection.')
    client.close()
    # Close the TCP socket
    srvSocket.close()
# end of main

if __name__ == '__main__':
    main()
    


