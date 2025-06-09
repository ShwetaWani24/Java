<Window x:Class="WpfClient.Views.LoginWindow"
        xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        xmlns:d="http://schemas.microsoft.com/expression/blend/2008"
        xmlns:mc="http://schemas.openxmlformats.org/markup-compatibility/2006"
        xmlns:local="clr-namespace:WpfClient.Views"
        mc:Ignorable="d"
        Title="LoginWindow" Height="250" Width="400">
    <Grid>
        <Grid.RowDefinitions>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
            <RowDefinition Height="Auto"/>
        </Grid.RowDefinitions>

        <TextBlock Text="Username: " Grid.Row="0" Margin="0 0 0 5"/>
        <TextBox x:Name="txtUserName" Grid.Row="1" Height="30"/>


        <TextBlock Text="Password: " Grid.Row="2" Margin="0 15 0 5"/>
        <PasswordBox  x:Name="txtPassword" Grid.Row="3" Margin="0,0,0,25" Grid.RowSpan="2"/>

        <Button Grid.Row="4" Content="Login" Width="100" Height="30"
                Margin="0,40,0,-20"  HorizontalAlignment="Center" Click="BtnLogin_Click"/>

    </Grid>
</Window>
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using WpfClient.Services;

namespace WpfClient.Views
{
    /// <summary>
    /// Interaction logic for LoginWindow.xaml
    /// </summary>
    public partial class LoginWindow : Window
    {
        public LoginWindow()
        {
            InitializeComponent();
        }

        private async void BtnLogin_Click(object sender, RoutedEventArgs e) {
            string username = txtUserName.Text.Trim();
            string password=txtPassword.Password.Trim();

            if(string.IsNullOrEmpty(username)  || string.IsNullOrEmpty(password))
            {
                MessageBox.Show("Please enter username and password");
                return;
            }

            string request = $"LOGIN|{username}|{password}";
            string response = await TcpClientService.SendRequestAsync(request);

            if (response == "SUCCESS")
            {
                MessageBox.Show("Login Successful!");
                StudentWindow studentWindow
                    = new StudentWindow();
                studentWindow.Show();
                this.Close();

            }
            else {
                MessageBox.Show("Login failed. Try again ");
            }

        }
    }
}
