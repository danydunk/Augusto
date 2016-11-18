# Echo client program
import socket
import sys

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
data = s.recv(2)
s.close()
print "received "+data
if data.strip() == "ok":
    exit(0)
else:
    exit(-1)
	