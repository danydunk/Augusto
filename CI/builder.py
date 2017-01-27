import sys
import os
import time

print "starting"
time.sleep(10)
file = open('E:\\.CI.data',  'r')
_id = file.read()
_id = _id.strip()
print "found "+_id
if len(_id) != 40 or _id == "ok" or _id =="ko" or _id == "running...":
    print "error"
    os.system("echo ko > E:\\.CI.data")
    exit(-1)
print "writing running"
os.system("echo running... > E:\\.CI.data")
print "removing dir"
os.system("rmdir /S /Q C:\\Users\\usi\\Desktop\\BUILDS\\Augusto")
print "pulling"
os.system("git clone https://github.com/danydunk/Augusto.git && cd C:\\Users\\usi\\Desktop\\BUILDS\\Augusto && git checkout "+_id)
print "building"
res = os.system("cd C:\\Users\\usi\\Desktop\\BUILDS\\Augusto && gradle testall > C:\\Users\\usi\\Desktop\\BUILDS\\"+_id+".out 2>&1")
os.system("copy /Y C:\\Users\\usi\\Desktop\\BUILDS\\"+_id+".out E:\\.CIBUILD.out")
if res == 0:
    os.system("echo ok > E:\\.CI.data")
    os.system("rmdir C:\\Users\\usi\\Desktop\\BUILDS\\"+_id+".out /S /Q")
else:
    os.system("echo ko > E:\\.CI.data")
print "switching off" 
os.system("shutdown /s")

