###############關於資料庫中排行榜的功能
import mysql.connector
import conn

mydb = conn.mydb

mycursor = mydb.cursor(buffered=True)

def insert_rank(name, score, updatetime):
    sql = "INSERT INTO rank (name, score, updatetime) VALUES (%s, %s, %s)"
    val = (name, score, updatetime)
    mycursor.execute(sql, val)
    
    mydb.commit()

    print(mycursor.rowcount, "record inserted.")

def print_rank():
    sql = "SELECT * FROM rank ORDER BY score DESC"
    
    mycursor.execute(sql)
    
    result = mycursor.fetchmany(10)
    return result
    # print(result)
# mycursor.execute("ALTER TABLE rank ADD COLUMN updatetime DATETIME") #新增上傳日期
