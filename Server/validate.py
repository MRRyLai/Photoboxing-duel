import re

def htmlspecialchars(data):
    return (
        data.replace("&", "&amp;").
        replace('"', "&quot;").
        replace("<", "&lt;").
        replace(">", "&gt;")
    )

def stripslashes(s):
    r = re.sub(r"\\(n|r)", "\n", s)
    r = re.sub(r"\\", "", r)
    return r

def validate(data):
    
    data = data.strip()
    data = stripslashes(data)
    data = htmlspecialchars(data)
    
    return data