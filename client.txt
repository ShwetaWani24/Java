using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using Serilog;

namespace ClientApp
{
    class Program
    {
        static Socket clientSocket;
        static bool connected = false;
        static Thread receiveThread;

        static void Main()
        {
            Log.Logger = new LoggerConfiguration()
                .WriteTo.File("clientlog.txt", rollingInterval: RollingInterval.Day)
                .CreateLogger();

            ConnectToServer();

            while (true)
            {
                if (!connected)
                {
                    Thread.Sleep(1000); // Avoid tight loop
                    continue;
                }

                string msgToSend = Console.ReadLine();
                if (string.IsNullOrWhiteSpace(msgToSend)) continue;

                try
                {
                    byte[] data = Encoding.ASCII.GetBytes(msgToSend);
                    clientSocket.Send(data);
                    Log.Information("Sent to server: {0}", msgToSend);
                }
                catch (Exception ex)
                {
                    Log.Error("Send failed: {0}", ex.Message);
                    connected = false;
                    Cleanup();
                    ConnectToServer();
                }
            }
        }

        static void ConnectToServer()
        {
            while (!connected)
            {
                try
                {
                    clientSocket?.Dispose();
                    clientSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                    clientSocket.Connect(IPAddress.Parse("127.0.0.1"), 1234);
                    connected = true;
                    Console.WriteLine("Connected to server.");
                    Log.Information("Connected to server.");

                    // Start new receive thread
                    receiveThread = new Thread(ReceiveMessages);
                    receiveThread.IsBackground = true;
                    receiveThread.Start();
                }
                catch
                {
                    Console.WriteLine("Retrying connection in 3 seconds...");
                    Log.Warning("Failed to connect. Retrying...");
                    Thread.Sleep(3000);
                }
            }
        }

        static void ReceiveMessages()
        {
            try
            {
                while (connected)
                {
                    byte[] buffer = new byte[1024];
                    int received = clientSocket.Receive(buffer);

                    if (received == 0)
                        throw new SocketException();

                    string message = Encoding.ASCII.GetString(buffer, 0, received);
                    Console.WriteLine("Server: " + message);
                    Log.Information("Received from server: {0}", message);
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine("Disconnected from server. Trying to reconnect...");
                Log.Warning("Lost connection: {0}", ex.Message);
                connected = false;
                Cleanup();
                ConnectToServer();
            }
        }

        static void Cleanup()
        {
            try
            {
                clientSocket?.Shutdown(SocketShutdown.Both);
                clientSocket?.Close();
            }
            catch { }
        }
    }
}
