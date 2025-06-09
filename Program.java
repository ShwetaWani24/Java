Folder Structure:
- Services
  - RequestHandler.cs
- Program.cs
- App.config (for connection string)

====================
Program.cs
====================
TcpListener listener = new TcpListener(IPAddress.Any, 9000);
listener.Start();
Console.WriteLine("Server started...");

while (true)
{
    TcpClient client = listener.AcceptTcpClient();
    _ = Task.Run(() => HandleClient(client));
}

void HandleClient(TcpClient client)
{
    using NetworkStream stream = client.GetStream();
    byte[] buffer = new byte[1024];
    int bytesRead = stream.Read(buffer, 0, buffer.Length);
    string request = Encoding.UTF8.GetString(buffer, 0, bytesRead);

    string response = RequestHandler.ProcessRequest(request);

    byte[] responseData = Encoding.UTF8.GetBytes(response);
    stream.Write(responseData, 0, responseData.Length);
    client.Close();
}

====================
Services/RequestHandler.cs
====================
public class RequestHandler
{
    private static string connectionString = ConfigurationManager.ConnectionStrings["db"].ConnectionString;

    public static string ProcessRequest(string request)
    {
        string[] parts = request.Split('|');
        string command = parts[0];

        switch (command)
        {
            case "LOGIN":
                return Login(parts[1], parts[2]);
            case "INSERT":
                return InsertStudent(parts[1], parts[2], parts[3]);
            case "UPDATE":
                return UpdateStudent(parts[1], parts[2], parts[3]);
            case "DELETE":
                return DeleteStudent(parts[1]);
            default:
                return "INVALID COMMAND";
        }
    }

    private static string Login(string username, string password)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("SELECT COUNT(*) FROM Users WHERE Username=@u AND Password=@p", conn);
        cmd.Parameters.AddWithValue("@u", username);
        cmd.Parameters.AddWithValue("@p", password);
        int count = (int)cmd.ExecuteScalar();
        return count > 0 ? "SUCCESS" : "FAIL";
    }

    private static string InsertStudent(string id, string name, string age)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("INSERT INTO Students VALUES(@id, @name, @age)", conn);
        cmd.Parameters.AddWithValue("@id", id);
        cmd.Parameters.AddWithValue("@name", name);
        cmd.Parameters.AddWithValue("@age", age);
        return cmd.ExecuteNonQuery() > 0 ? "SUCCESS" : "ERROR";
    }

    private static string UpdateStudent(string id, string name, string age)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("UPDATE Students SET Name=@name, Age=@age WHERE Id=@id", conn);
        cmd.Parameters.AddWithValue("@id", id);
        cmd.Parameters.AddWithValue("@name", name);
        cmd.Parameters.AddWithValue("@age", age);
        return cmd.ExecuteNonQuery() > 0 ? "SUCCESS" : "ERROR";
    }

    private static string DeleteStudent(string id)
    {
        using SqlConnection conn = new SqlConnection(connectionString);
        conn.Open();
        SqlCommand cmd = new SqlCommand("DELETE FROM Students WHERE Id=@id", conn);
        cmd.Parameters.AddWithValue("@id", id);
        return cmd.ExecuteNonQuery() > 0 ? "SUCCESS" : "ERROR";
    }
}

=====================
App.config
=====================
<configuration>
  <connectionStrings>
    <add name="db" connectionString="Data Source=.;Initial Catalog=StudentDB;Integrated Security=True" />
  </connectionStrings>
</configuration>
