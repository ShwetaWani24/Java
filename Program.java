// Solution Structure:
// - Solution: StudentManagementSystem
//   - Project 1: TCPServerLibrary (Console App)
//   - Project 2: WpfClient (WPF App)

//-----------------------------------
// Project: TCPServerLibrary
//-----------------------------------

// Program.cs
using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
using System.Data.SqlClient;

class Program
{
    static void Main()
    {
        TcpListener listener = new TcpListener(IPAddress.Any, 9000);
        listener.Start();
        Console.WriteLine("TCP Server started on port 9000...");

        while (true)
        {
            TcpClient client = listener.AcceptTcpClient();
            Task.Run(() => HandleClient(client));
        }
    }

    static async Task HandleClient(TcpClient client)
    {
        NetworkStream stream = client.GetStream();
        byte[] buffer = new byte[1024];
        int bytesRead = await stream.ReadAsync(buffer, 0, buffer.Length);
        string request = Encoding.UTF8.GetString(buffer, 0, bytesRead);

        string response = RequestHandler.Handle(request);
        byte[] responseBytes = Encoding.UTF8.GetBytes(response);
        await stream.WriteAsync(responseBytes, 0, responseBytes.Length);
        client.Close();
    }
}

// RequestHandler.cs
public static class RequestHandler
{
    private static string connectionString = "your_connection_string_here";

    public static string Handle(string request)
    {
        string[] parts = request.Split('|');
        string action = parts[0].ToUpper();

        switch (action)
        {
            case "LOGIN":
                return HandleLogin(parts[1], parts[2]);
            case "INSERT":
                return HandleInsert(parts[1], int.Parse(parts[2]));
            case "UPDATE":
                return HandleUpdate(int.Parse(parts[1]), parts[2], int.Parse(parts[3]));
            case "DELETE":
                return HandleDelete(int.Parse(parts[1]));
            default:
                return "INVALID";
        }
    }

    private static string HandleLogin(string username, string password)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("SELECT COUNT(*) FROM Users WHERE Username=@u AND Password=@p", conn);
        cmd.Parameters.AddWithValue("@u", username);
        cmd.Parameters.AddWithValue("@p", password);

        int count = (int)cmd.ExecuteScalar();
        return count == 1 ? "SUCCESS" : "FAILURE";
    }

    private static string HandleInsert(string name, int age)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("INSERT INTO Student (Name, Age) VALUES (@n, @a)", conn);
        cmd.Parameters.AddWithValue("@n", name);
        cmd.Parameters.AddWithValue("@a", age);
        return cmd.ExecuteNonQuery() == 1 ? "INSERTED" : "ERROR";
    }

    private static string HandleUpdate(int id, string name, int age)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("UPDATE Student SET Name=@n, Age=@a WHERE Id=@i", conn);
        cmd.Parameters.AddWithValue("@n", name);
        cmd.Parameters.AddWithValue("@a", age);
        cmd.Parameters.AddWithValue("@i", id);
        return cmd.ExecuteNonQuery() == 1 ? "UPDATED" : "ERROR";
    }

    private static string HandleDelete(int id)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("DELETE FROM Student WHERE Id=@i", conn);
        cmd.Parameters.AddWithValue("@i", id);
        return cmd.ExecuteNonQuery() == 1 ? "DELETED" : "ERROR";
    }
}

//-----------------------------------
// Project: WpfClient
//-----------------------------------

// TcpClientService.cs
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace WpfClient.Services
{
    public static class TcpClientService
    {
        public static async Task<string> SendRequestAsync(string request)
        {
            TcpClient client = new TcpClient();
            await client.ConnectAsync("127.0.0.1", 9000);
            NetworkStream stream = client.GetStream();

            byte[] data = Encoding.UTF8.GetBytes(request);
            await stream.WriteAsync(data, 0, data.Length);

            byte[] buffer = new byte[1024];
            int bytesRead = await stream.ReadAsync(buffer, 0, buffer.Length);

            return Encoding.UTF8.GetString(buffer, 0, bytesRead);
        }
    }
}

// LoginWindow.xaml.cs
using System.Windows;
using WpfClient.Services;

namespace WpfClient.Views
{
    public partial class LoginWindow : Window
    {
        public LoginWindow() => InitializeComponent();

        private async void BtnLogin_Click(object sender, RoutedEventArgs e)
        {
            string username = txtUserName.Text.Trim();
            string password = txtPassword.Password.Trim();

            string response = await TcpClientService.SendRequestAsync($"LOGIN|{username}|{password}");
            if (response == "SUCCESS")
            {
                new StudentWindow().Show();
                this.Close();
            }
            else
            {
                MessageBox.Show("Login failed.");
            }
        }
    }
}

// StudentWindow.xaml.cs
using System.Windows;
using WpfClient.Services;

namespace WpfClient.Views
{
    public partial class StudentWindow : Window
    {
        public StudentWindow() => InitializeComponent();

        private async void BtnInsert_Click(object sender, RoutedEventArgs e)
        {
            string name = txtName.Text;
            int age = int.Parse(txtAge.Text);
            string response = await TcpClientService.SendRequestAsync($"INSERT|{name}|{age}");
            MessageBox.Show(response);
        }

        private async void BtnUpdate_Click(object sender, RoutedEventArgs e)
        {
            int id = int.Parse(txtId.Text);
            string name = txtName.Text;
            int age = int.Parse(txtAge.Text);
            string response = await TcpClientService.SendRequestAsync($"UPDATE|{id}|{name}|{age}");
            MessageBox.Show(response);
        }

        private async void BtnDelete_Click(object sender, RoutedEventArgs e)
        {
            int id = int.Parse(txtId.Text);
            string response = await TcpClientService.SendRequestAsync($"DELETE|{id}");
            MessageBox.Show(response);
        }
    }
}
