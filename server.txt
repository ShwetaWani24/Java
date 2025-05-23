using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Serilog;

namespace ServerApp
{
    class Program
    {
        static Socket clientSocket;

        static void Main()
        {
            Log.Logger = new LoggerConfiguration()
                .WriteTo.File("serverlog.txt", rollingInterval: RollingInterval.Day)
                .CreateLogger();

            try
            {
                Socket serverSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                serverSocket.Bind(new IPEndPoint(IPAddress.Any, 1234));
                serverSocket.Listen(10);

                Console.WriteLine("Server started. Waiting for client...");
                clientSocket = serverSocket.Accept();
                Console.WriteLine("Client connected.");

                Thread receiveThread = new Thread(ReceiveMessages);
                receiveThread.Start();

                while (true)
                {
                    string msgToSend = Console.ReadLine();
                    byte[] data = Encoding.ASCII.GetBytes(msgToSend);
                    clientSocket.Send(data);
                    Log.Information("Sent to client: {0}", msgToSend);
                }
            }
            catch (Exception ex)
            {
                Log.Error("Server error: {0}", ex.Message);
            }
        }

        static void ReceiveMessages()
        {
            try
            {
                while (true)
                {
                    byte[] buffer = new byte[1024];
                    int received = clientSocket.Receive(buffer);
                    string message = Encoding.ASCII.GetString(buffer, 0, received);
                    Console.WriteLine("Client: " + message);
                    Log.Information("Received from client: {0}", message);
                }
            }
            catch (Exception ex)
            {
                Log.Error("Receive error: {0}", ex.Message);
            }
        }
    }
}
