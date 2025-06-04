using System.Data;
using System;
using System.Data.SqlClient;
using System.IO;
using ExcelDataReader;


namespace BulkInsert
{
     class Program
    {
    

        //private static string filePath = "C:\\Users\\shwetaw\\Documents\\EmployeeData.xlsx";
        private static string connectionString = "Data Source=10.100.1.57;Initial Catalog=Sample1;User ID=sa;Password=Sa@123;TrustServerCertificate=True;";
        static void Main(string[] args)
        {
            System.Text.Encoding.RegisterProvider(System.Text.CodePagesEncodingProvider.Instance);
            string filePath = "C:\\Users\\shwetaw\\Downloads\\Employee.xlsx";
            try
            {
                Console.WriteLine("Reading Excel File..........");

                DataTable dataTable = ReadExcelFile(filePath);


                Console.WriteLine("Inserting data into Sql");

                InsertDataIntoSql(dataTable);
                Console.WriteLine("Data Inserted Succesfully");



            }
            catch(Exception ex) { 
               Console.WriteLine(ex.Message);
            
            
            }    

            Console.WriteLine("Press any key to exit");
            Console.ReadKey();  
            
            
        }

        private static DataTable ReadExcelFile(string path)
        {
            using var stream=File.Open(path,FileMode.Open,FileAccess.Read);
            using var reader=ExcelReaderFactory.CreateReader(stream);

            var result = reader.AsDataSet(new ExcelDataSetConfiguration()
            {
                ConfigureDataTable = (_) => new ExcelDataTableConfiguration()
                {
                    UseHeaderRow = true

                }

            });
            return result.Tables[0];

        }

        private static void InsertDataIntoSql(DataTable dt) { 
           using SqlConnection conn=new SqlConnection(connectionString);
            conn.Open();
            foreach (DataRow row in dt.Rows)
            {  
                string empCode = row["EmployeeCode"].ToString();
                string name = row["Name"].ToString();
                int age = Convert.ToInt32(row["Age"]);
                string department = row["Department"].ToString();

               
                string checkQuery = "SELECT COUNT(*) FROM EmployeeData1 WHERE EmployeeCode = @EmpCode";
                using SqlCommand checkCmd = new SqlCommand(checkQuery, conn);
                checkCmd.Parameters.AddWithValue("@EmpCode", empCode);
                int count = (int)checkCmd.ExecuteScalar();

                if (count > 0)
                {
                   
                    string updateQuery = "UPDATE EmployeeData1 SET Name= @Name, Age = @Age, Department = @Department WHERE EmployeeCode=@EmpCode";
                    using SqlCommand updateCmd = new SqlCommand(updateQuery, conn);
                    updateCmd.Parameters.AddWithValue("@Name", name);
                    updateCmd.Parameters.AddWithValue("@Age", age);
                    updateCmd.Parameters.AddWithValue("@Department", department);
                    updateCmd.Parameters.AddWithValue("@EmpCode", empCode);
                    updateCmd.ExecuteNonQuery();
                }
                else
                {
       
                    string insertQuery = "INSERT INTO EmployeeData (EmployeeCode,Name, Age, Department) VALUES (@EmpCode, @Name, @Age, @Department)";
                    using SqlCommand insertCmd = new SqlCommand(insertQuery, conn);
                    insertCmd.Parameters.AddWithValue("@EmpCode", empCode);
                    insertCmd.Parameters.AddWithValue("@Name", name);
                    insertCmd.Parameters.AddWithValue("@Age", age);
                    insertCmd.Parameters.AddWithValue("@Department", department);
                    insertCmd.ExecuteNonQuery();
                }
            }
        }
    }
}
