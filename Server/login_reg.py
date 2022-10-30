######################登入、註冊系統
import mysql.connector
import conn
import validate
import cgi
import hashlib

# serverIP = '127.0.0.1'
# PORT = 6666

# URL = 'http://' + str(serverIP) + ':' + str(PORT) + '/forums'


# form = cgi.FieldStorage()
mydb = conn.mydb

mycursor = mydb.cursor(buffered=True)

def Login(email, password):
    if(email!="" and password!=""):
        password_hash = hashlib.md5(password.encode('utf-8')).hexdigest()
        sql = "SELECT * FROM users where (email = %s) and (password = %s)"
        adr = (email, password_hash)
        mycursor.execute(sql,adr)
        
        result = mycursor.fetchall()
        print(result)
        StrResult = " ".join(map(str, result))
        StrResult = StrResult.replace("(", ", ").replace(")", "").replace("'", "")
        print(StrResult)
        if(len(result)>0):
            return "success"+StrResult+"\n"
        else:
            return "failure\n"
    else :
        return "failure\n"

def Register(name, email, password):
    if(name!="" and email!="" and password!=""):
        
        
        password_hash = hashlib.md5(password.encode('utf-8')).hexdigest()
        
        sql = "INSERT INTO users (name, email, password) VALUES (%s, %s, %s)"
        adr = (name, email, password_hash)
        try:
            mycursor.execute(sql, adr)
            mydb.commit()
        except mysql.connector.Error as e:
            return "duplicate e-mail\n"
        return "success\n"

    else :
        return "failure\n"
