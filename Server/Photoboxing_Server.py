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
import login_reg
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
waitinggamestack = []
userid = []
conn_pool = []
addr_list = []
list_of_dfs = []
stage = ["login", "register", "score", "rank", "playerwaiting", "openpose"]#目前的階段
sleeptime = 3

def extract_ip():
    st = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    try:       
        st.connect(('10.255.255.255', 1))
        
        HOST = st.getsockname()[0]
        print("HOST:")
        print(HOST)
    except Exception:
        HOST = '127.0.0.1'
    finally:
        st.close()
    return HOST


class ServerThread(threading.Thread):
    def __init__(self, t_name, client_sc, rip, rport):
        super().__init__(name = t_name)
        self.client = client_sc
        self.rip = rip
        self.value=0
        self.username = "username"
        self.email = "@gmail.com"
        self.gamemode = "mode"
        self.rport = rport
        self.start()            # Start the thread when it is created

    # end for __init__()
    
    def run(self):
        name = threading.current_thread().name
        time_limit = 0
        try:
            # self.data = self.client.recv(BUF_SIZE).decode('utf-8')
            # self.client.send("GetConnection.\n".encode('utf-8'))
            while True:
                client_stage=""
                self.data = self.client.recv(BUF_SIZE).decode('utf-8')
                msg = self.data.split('\n')
                client_stage = msg[0]
                #########################################
                # print("self.data:")
                # print(self.data)
                # print("msg:")
                # print(msg)
                # print("client_stage:")
                # print(client_stage)
                # print("self.stage:")
                # print(self.stage)
                ############################################################
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
                    
                    print('Waiting for message from client...')
                    # self.data = self.client.recv(BUF_SIZE).decode('utf-8')
                    # print('Send msg to Client:%s'%self.data)
                    
                    
                else:
                    print(stage[0])
                    if(client_stage==stage[0]):# 判斷是否為登入階段
                        #此部分為將進行 登入部分:email、password  註冊部分:name、email、password 資料的接收
                        #接收後，資料核對資料庫，如果有就回傳success給app沒有就傳failure
                        self.email = msg[1]
                        self.password = msg[2]
                        print("self.email:")
                        print(self.email)
                        print("self.password:")
                        print(self.password)
                        result = login_reg.Login(self.email, self.password)
                        print(result)
                        self.client.send(result.encode('utf-8'))
                        break
                    elif(client_stage==stage[1]):#註冊階段
                        self.username = msg[1]
                        self.email = msg[2]
                        self.password = msg[3]
                        print("self.username:")
                        print(self.username)
                        print("self.email:")
                        print(self.email)
                        print("self.password:")
                        print(self.password)
                        result = login_reg.Register(self.username, self.email, self.password)
                        print(result)
                        self.client.send(result.encode('utf-8'))
                        break
                    elif(client_stage==stage[2]):# 分數階段
                        # count=1
                        time_limit = 0
                        # print("length(self.data):%d message:%s\n"%(len(self.data),self.data))
                        self.email = msg[1]
                        self.name = msg[2]
                        self.value = int(msg[3])
                        
                        ###############################################################將數據傳到資料庫rank裡
                        
                        print(self.email)
                        print(self.name)
                        print(self.value)
                        nowtime = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                        
                        Database_rank.insert_rank(self.name, self.value, nowtime)
                        # rank_list = Database_rank.print_rank()
                        # rank_df = pd.DataFrame(rank_list, columns = ['Name', 'Score', 'Updatetime'])
                        # print(rank_df[0:10])
                        # rank_df = rank_df.astype('string')
                        # # print(rank_df.dtypes)
                        # rank_str = rank_df[0:10]
                        # ###############################################################
                        
                        # self.email = self.data[0:11]
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
                        
                        # 將int轉換成string傳送回去
                        # print((self.rip, self.rport))
                        # print(name)
                        # print('Get message:%s\n' %self.data)
                        # print('value:%d\n'%self.value)
                        # cur_thread = threading.current_thread()
                        stack.append((self.name,self.value))
                        stack.reverse()
                        print('User:%s put %d in stack'%(self.name,self.value))
                        cur_thread = threading.current_thread()
                        while(len(stack)>0):
                            i=0
                            for i in stack:
                                # print('i:')
                                # print(i)
                                # 05/18目前的功能是能夠將資料互傳但是要再多一步動作傳輸
                                # 舉例:A傳入B傳入會立即收到A資料，但是A要再傳入一次才能收到B資料(已解決)
                                if(i[0]==self.name):
                                    # print('Waiting for message from client...')
                                    pass
                                else:
                                    print('i[0]:%s i[1]:%d'%(i[0],i[1]))
                                    self.data = i[0]+", "+str(i[1])
                                    something = str(stack.pop())
                                    print("something:%s"%something)
                                    self.data = self.data+"\n"
                                    print(self.data)
                                    self.client.send(self.data.encode('utf-8'))
                                    print('Send msg to Client.')
                                    # print("Now in list:%s"%str(stack))
                                    print('Waiting for message from client...')
                                    break
                        break
                        #########################################################
                        #做成loop
                    elif(client_stage==stage[3]):#排名階段
                        print("In Rank.")
                        rank_list = Database_rank.print_rank()
                        rank_df = pd.DataFrame(rank_list, columns = ['Name', 'Score', 'Updatetime'])
                        print(rank_df["Name"])
                        list_of_name = rank_df['Name'].tolist()
                        list_of_score = rank_df['Score'].tolist()
                        list_of_updatetime = rank_df['Updatetime'].tolist()
                        # print(rank_df.dtypes)
                        print(list_of_name)
                        print(list_of_score)
                        print(list_of_updatetime)
                        rank_str = ' '.join(list_of_name)
                        print(rank_str+"\n")
                        rank_str = rank_str+","+' '.join(str(x) for x in list_of_score)
                        print(rank_str+"\n")
                        rank_str = rank_str+","+';'.join(str(y) for y in list_of_updatetime)
                        print(rank_str+"\n")
                        rank_str = rank_str+"\n"
                        self.client.send(rank_str.encode('utf-8'))
                    elif(client_stage==stage[4]):#等待玩家階段
                        print("\nIn PlayerWaiting.")
                        self.username = msg[1]
                        self.email = msg[2]
                        self.gamemode = msg[3]
                        if(self.username != "username" and self.email != "@gmail.com"):
                            waitinggamestack.append((self.gamemode,self.username))
                            waitinggamestack.reverse()
                            print('Gamemode:%s and %s in stack'%(self.gamemode,self.username))
                            cur_thread = threading.current_thread()
                            while(len(waitinggamestack)>0):
                                i=0
                                for i in waitinggamestack:
                                    if(i[1]==self.username):
                                        # print('Waiting for message from client...')
                                        pass
                                    else:
                                        print('i[0]:%s i[1]:%s'%(i[0],i[1]))
                                        self.data = i[0]+", "+i[1]
                                        something = str(waitinggamestack.pop())
                                        print("something:%s"%something)
                                        self.data = self.data+"\n"
                                        print(self.data)
                                        self.client.send(self.data.encode('utf-8'))
                                        print('Send msg to Client.')
                                        # print("Now in list:%s"%str(stack))
                                        print('Waiting for message from client...')
                                        break
                    else:#完成回傳資料部分
                        break
                        
                if(time_limit>=15):
                    print("Time is out!")
                    break
        except KeyboardInterrupt:
            print ('KeyboardInterrupt exception is caught')
        cur_thread = threading.current_thread()
        addr_list.remove((self.rip,self.rport))
        for item in addr_list:
            print(item[1])
        self.client.close()
        print(name, 'Thread closed')
        # end run()
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
    extract_ip()
    # print(socket.gethostbyname(socket.gethostname()))
    print(f"Hostname: {hostname}")
    print(f"IP Address: {ip_address}")
    # Enable reuse address/port
    #srvSocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    
    # Bind  on any incoming interface with PORT, '' is any interface
    try:
        srvSocket.bind(("192.168.0.10", PORT))
    # 26.72.187.29 VPN
    # 192.168.0.10 家用
    # 192.168.43.241 手機
    
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
            print('Now working threads: %d' % (threading.active_count()-1))
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
    


