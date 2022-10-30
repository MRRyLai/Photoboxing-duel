import mysql.connector

#完成了建立資料表以及手動插入資料

mydb = mysql.connector.connect(
  host="localhost",
  user="root",
  password="",
  database="register_from_android"
)

mycursor = mydb.cursor()
# mycursor.execute("ALTER TABLE users ADD COLUMN name VARCHAR PRIMARY KEY")
def insert (player1, player2, socre1, score2):
    sql = "INSERT INTO battle_record (id1, id2 , score1, score2) VALUES (%s, %s, %s, %s)"
    val = ( "3", "4", "1", "25")
    mycursor.execute(sql, val)
    mydb.commit()

def select (table):
    mycursor.execute("SELECT * FROM %s"%table)
    myresult = mycursor.fetchall()

def delete(table):
    sql = "DELETE FROM battle_record WHERE object"
    mycursor.execute(sql)
    mydb.commit()
    print(mycursor.rowcount, "record(s) deleted")
    mycursor.execute("SHOW TABLES")
    for x in mycursor:
        print(x)