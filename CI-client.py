# Echo client program
import socket
import sys
import time
import os

def getPacket(s):
	s.read()
	
print "starting"
arg = sys.argv[1]
arg2 = sys.argv[2]

HOST = 'research.inf.usi.ch'    # The remote host
PORT = 50000              # The same port as used by the server
	
started = time.time()

if arg2 == "first":
	s = None
	for res in socket.getaddrinfo(HOST, PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
		af, socktype, proto, canonname, sa = res
		try:
			s = socket.socket(af, socktype, proto)
		except socket.error as msg:
			s = None
			continue
		try:
			s.connect(sa)
		except socket.error as msg:
			s.close()
			s = None
			continue
		break
	if s is None:
		print "could not open socket"
		sys.exit(-1)
	s.sendall(arg)
	data = s.recv(20).strip()
	s.close()
	if not(data == 'started'):
		print data
		exit(-1)

while 1:
	s = None
	for res in socket.getaddrinfo(HOST, PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
		af, socktype, proto, canonname, sa = res
		try:
			s = socket.socket(af, socktype, proto)
		except socket.error as msg:
			s = None
			continue
		try:
			s.connect(sa)
		except socket.error as msg:
			s.close()
			s = None
			continue
		break
	if s is None:
		print "could not open socket"
		sys.exit(-1)
	s.sendall("gimmeresult")
	msg = ""
	while True:
		received = s.recv(1024)
		if not received:
			break	
		msg += received
	s.close()
	msg.strip()
	if msg == 'running':
		if (time.time() - started) > 2700:
			print 'timeout'
			exit(0)
		continue
		
	if msg == "error - not testing":
		print "Testing was finished"
		exit(0)
		
	if msg.startswith('error'):
		print msg
		exit(-1)
	assert(msg.endswith("CIBUILD=OK") or msg.endswith("CIBUILD=KO"))

	print "Logs:"
	print msg.strip("CIBUILD=OK").strip("CIBUILD=KO")
	
	if msg.endswith("CIBUILD=OK"):
		exit(0)
	else:
		exit(-1)