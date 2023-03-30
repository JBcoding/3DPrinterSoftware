import socket

HOST = "localhost"  # Standard loopback interface address (localhost)
PORT = 54321  # Port to listen on (non-privileged ports are > 1023)

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.bind((HOST, PORT))
    s.listen()
    while True:
        conn, addr = s.accept()
        with conn:
            print(f"Connected by {addr}")
            total_data = None
            while True:
                data = conn.recv(1024)
                if not total_data:
                    total_data = data.decode("utf-8")
                else:
                    total_data += data.decode("utf-8")
                if "MOB-EOF" in total_data:
                    break
            exec(total_data)
            if solutionWithSmallest is not None and solutionWithLargest is not None:
                output = "{}\n{}\n{}\n{}".format('{:.20f}'.format(solutionWithSmallest[0]), '{:.20f}'.format(solutionWithSmallest[1]), '{:.20f}'.format(solutionWithLargest[0]), '{:.20f}'.format(solutionWithLargest[1]))
                conn.sendall(output.encode('utf-8'))
            else:
                print("No solution")
            conn.close()