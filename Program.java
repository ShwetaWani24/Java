using System;
using System.Data;
using System.Data.SqlClient;
using System.Windows;

namespace WpfAdoApp
{
    public partial class MainWindow : Window
    {
        private readonly string connectionString = "Server=localhost\\SQLEXPRESS;Database=WpfAdoAppDb;Trusted_Connection=True;";

        public MainWindow()
        {
            InitializeComponent();
            LoadPeople();
        }

        private void LoadPeople()
        {
            try
            {
                using (SqlConnection con = new SqlConnection(connectionString))
                {
                    string query = "SELECT * FROM People ORDER BY LastModified DESC";
                    SqlDataAdapter adapter = new SqlDataAdapter(query, con);
                    DataTable dt = new DataTable();
                    adapter.Fill(dt);
                    PeopleDataGrid.ItemsSource = dt.DefaultView;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error loading people: " + ex.Message);
            }
        }

        private void Add_Click(object sender, RoutedEventArgs e)
        {
            if (string.IsNullOrWhiteSpace(NameTextBox.Text))
            {
                MessageBox.Show("Please enter a name.");
                return;
            }

            try
            {
                using (SqlConnection con = new SqlConnection(connectionString))
                {
                    con.Open();
                    string query = "INSERT INTO People (Name, LastModified) VALUES (@Name, GETDATE())";
                    SqlCommand cmd = new SqlCommand(query, con);
                    cmd.Parameters.AddWithValue("@Name", NameTextBox.Text.Trim());
                    cmd.ExecuteNonQuery();
                }

                NameTextBox.Clear();
                LoadPeople();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error adding person: " + ex.Message);
            }
        }

        private void Delete_Click(object sender, RoutedEventArgs e)
        {
            if (PeopleDataGrid.SelectedItem == null)
            {
                MessageBox.Show("Please select a row to delete.");
                return;
            }

            DataRowView row = PeopleDataGrid.SelectedItem as DataRowView;
            int id = Convert.ToInt32(row["Id"]);

            try
            {
                using (SqlConnection con = new SqlConnection(connectionString))
                {
                    con.Open();
                    string query = "DELETE FROM People WHERE Id = @Id";
                    SqlCommand cmd = new SqlCommand(query, con);
                    cmd.Parameters.AddWithValue("@Id", id);
                    cmd.ExecuteNonQuery();
                }

                LoadPeople();
            }
            catch (Exception ex)
            {
                MessageBox.Show("Error deleting person: " + ex.Message);
            }
        }
    }
}


