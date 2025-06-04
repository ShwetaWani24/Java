foreach (DataRow row in dt.Rows)
{
    string name = row["Name"].ToString();
    int age = Convert.ToInt32(row["Age"]);
    string department = row["Department"].ToString();

    // First, check if record with same Name exists
    string checkQuery = "SELECT COUNT(*) FROM EmployeeData WHERE Name = @Name";
    using SqlCommand checkCmd = new SqlCommand(checkQuery, conn);
    checkCmd.Parameters.AddWithValue("@Name", name);
    int count = (int)checkCmd.ExecuteScalar();

    if (count > 0)
    {
        // Record exists: Update it
        string updateQuery = "UPDATE EmployeeData SET Age = @Age, Department = @Department WHERE Name = @Name";
        using SqlCommand updateCmd = new SqlCommand(updateQuery, conn);
        updateCmd.Parameters.AddWithValue("@Name", name);
        updateCmd.Parameters.AddWithValue("@Age", age);
        updateCmd.Parameters.AddWithValue("@Department", department);
        updateCmd.ExecuteNonQuery();
    }
    else
    {
        // Record does not exist: Insert it
        string insertQuery = "INSERT INTO EmployeeData (Name, Age, Department) VALUES (@Name, @Age, @Department)";
        using SqlCommand insertCmd = new SqlCommand(insertQuery, conn);
        insertCmd.Parameters.AddWithValue("@Name", name);
        insertCmd.Parameters.AddWithValue("@Age", age);
        insertCmd.Parameters.AddWithValue("@Department", department);
        insertCmd.ExecuteNonQuery();
    }
}
