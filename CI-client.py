# Echo client program
import socket
import sys

def getPacket(s):
	s.read()
	


arg = sys.argv[1]

HOST = 'research.inf.usi.ch'    # The remote host
PORT = 50000              # The same port as used by the server
s = None
for res in socket.getaddrinfo(HOST, PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
    af, socktype, proto, canonname, sa = res
    try:
        s = socket.socket(af, socktype, proto)
    except socket.error as msg:
        s = None
        continue
    try:
		s.settimeout(18000)
		s.connect(sa)
    except socket.error as msg:
        s.close()
        s = None
        continue
    break
if s is None:
    print "could not open socket"
    sys.exit(1)
s.sendall(arg)

msg = ""
while True:
	received = s.recv(1024)
	if not received:
		break	
	msg += received
s.close()
print msg
assert(msg.endswith("CIBUILD=OK") or msg.endswith("CIBUILD=KO"))

print "Logs:"
print msg.strip("CIBUILD=OK").strip("CIBUILD=KO")
	
if msg.endswith("CIBUILD=OK"):
	exit(0)
else:
	exit(-1)