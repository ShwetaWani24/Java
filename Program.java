<Window x:Class="CrudWpfApp.MainWindow"
        xmlns="http://schemas.microsoft.com/winfx/2006/xaml/presentation"
        xmlns:x="http://schemas.microsoft.com/winfx/2006/xaml"
        Title="CRUD Operation in C# (WPF)" Height="450" Width="700">
    <Grid Margin="10">
        <Label Content="CRUD Operation in C# (WPF)" FontSize="16" HorizontalAlignment="Center" Grid.Row="0" Margin="0,0,0,10"/>

        <StackPanel Orientation="Vertical" HorizontalAlignment="Left" VerticalAlignment="Top">
            <Label Content="Name"/>
            <TextBox x:Name="txtName" Width="200"/>

            <Label Content="Age"/>
            <TextBox x:Name="txtAge" Width="200"/>

            <Label Content="Gender"/>
            <TextBox x:Name="txtGender" Width="200"/>

            <Label Content="City"/>
            <TextBox x:Name="txtCity" Width="200"/>
        </StackPanel>

        <DataGrid x:Name="dataGrid" HorizontalAlignment="Right" Width="400" Height="250"
                  AutoGenerateColumns="True" Margin="10,10,0,10" SelectionMode="Single"
                  VerticalAlignment="Top" />

        <StackPanel Orientation="Horizontal" HorizontalAlignment="Center" VerticalAlignment="Bottom" Margin="0,10,0,0">
            <Button Content="Insert Record" Width="120" Margin="5" Background="#D96666" Click="Insert_Click"/>
            <Button Content="Update Record" Width="120" Margin="5" Background="#D96666" Click="Update_Click"/>
            <Button Content="Delete Record" Width="120" Margin="5" Background="#D96666" Click="Delete_Click"/>
            <Button Content="Clear Data" Width="120" Margin="5" Background="#D96666" Click="Clear_Click"/>
        </StackPanel>
    </Grid>
</Window>
