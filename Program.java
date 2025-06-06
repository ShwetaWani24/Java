using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.IO;
using System.Linq;
using System.Threading;
using System.Timers;

namespace CSVWatcher
{
    class Program
    {
        static string connectionString = "Data Source=YOUR_SERVER;Initial Catalog=YOUR_DATABASE;User ID=sa;Password=YOUR_PASSWORD;TrustServerCertificate=True;";
        static string tableName = "Product";
        static string procedureName = "UpsertProduct";
        static string watchFolder = @"C:\Users\YourName\Desktop\FILE";

        static HashSet<string> processedFiles = new HashSet<string>(StringComparer.OrdinalIgnoreCase);
        static System.Timers.Timer timer;

        static void Main(string[] args)
        {
            Console.WriteLine("📁 Watching folder: " + watchFolder);
            Console.WriteLine("🔁 Processing existing files...");

            // 1. Process existing CSV files
            CheckFolder(null, null);

            // 2. Start watcher for new files
            FileSystemWatcher watcher = new FileSystemWatcher
            {
                Path = watchFolder,
                Filter = "*.csv",
                NotifyFilter = NotifyFilters.FileName | NotifyFilters.LastWrite,
                EnableRaisingEvents = true
            };

            watcher.Created += OnFileCreated;
            watcher.Changed += OnFileChanged;

            // 3. Timer as fallback every 5 seconds
            timer = new System.Timers.Timer(5000);
            timer.Elapsed += CheckFolder;
            timer.Start();

            Console.WriteLine("✅ Ready. Drop your CSV files into the folder...");
            Console.ReadLine();
        }

        private static void OnFileCreated(object sender, FileSystemEventArgs e)
        {
            Console.WriteLine($"📄 New file created: {e.Name}");
            Thread.Sleep(1500); // Wait for file to finish writing
            ProcessFile(e.FullPath);
        }

        private static void OnFileChanged(object sender, FileSystemEventArgs e)
        {
            Console.WriteLine($"✏️ File changed: {e.Name}");
            Thread.Sleep(1500);
            ProcessFile(e.FullPath);
        }

        private static void CheckFolder(object sender, ElapsedEventArgs e)
        {
            var files = Directory.GetFiles(watchFolder, "*.csv");

            foreach (var file in files)
            {
                if (!processedFiles.Contains(file))
                {
                    Console.WriteLine($"🔎 Timer checking: {Path.GetFileName(file)}");
                    ProcessFile(file);
                }
            }
        }

        private static void ProcessFile(string fullPath)
        {
            if (processedFiles.Contains(fullPath)) return;

            try
            {
                if (!File.Exists(fullPath)) return;

                var lines = File.ReadAllLines(fullPath);
                if (lines.Length < 2)
                {
                    Console.WriteLine("⚠️ CSV does not contain data.");
                    processedFiles.Add(fullPath);
                    return;
                }

                var headers = lines[0].Split(',').Select(h => h.Trim()).ToList();
                var tableColumns = GetTableColumns(tableName);

                if (!headers.All(h => tableColumns.Contains(h, StringComparer.OrdinalIgnoreCase)))
                {
                    Console.WriteLine("❌ Header mismatch. Skipping: " + Path.GetFileName(fullPath));
                    processedFiles.Add(fullPath);
                    return;
                }

                for (int i = 1; i < lines.Length; i++)
                {
                    var values = lines[i].Split(',');
                    if (values.Length != headers.Count)
                    {
                        Console.WriteLine($"⚠️ Row {i + 1} column count mismatch. Skipping.");
                        continue;
                    }
                    CallStoredProcedure(headers, values);
                }

                Console.WriteLine($"✅ File processed: {Path.GetFileName(fullPath)}");
                processedFiles.Add(fullPath);
            }
            catch (Exception ex)
            {
                Console.WriteLine($"❌ Error processing file {Path.GetFileName(fullPath)}: {ex.Message}");
            }
        }

        private static List<string> GetTableColumns(string tableName)
        {
            var columns = new List<string>();
            using (SqlConnection conn = new SqlConnection(connectionString))
            {
                string query = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = @TableName";
                SqlCommand cmd = new SqlCommand(query, conn);
                cmd.Parameters.AddWithValue("@TableName", tableName);

                conn.Open();
                using (SqlDataReader reader = cmd.ExecuteReader())
                {
                    while (reader.Read())
                        columns.Add(reader.GetString(0));
                }
            }
            return columns;
        }

        private static void CallStoredProcedure(List<string> headers, string[] values)
        {
            using (SqlConnection conn = new SqlConnection(connectionString))
            using (SqlCommand cmd = new SqlCommand(procedureName, conn))
            {
                cmd.CommandType = CommandType.StoredProcedure;

                for (int i = 0; i < headers.Count; i++)
                {
                    cmd.Parameters.AddWithValue("@" + headers[i], values[i]);
                }

                conn.Open();
                cmd.ExecuteNonQuery();
                conn.Close();
            }
        }
    }
}
