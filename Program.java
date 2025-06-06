CREATE PROCEDURE UpsertProduct
    @ProductID INT,
    @ProductName NVARCHAR(100),
    @Category NVARCHAR(50),
    @Price DECIMAL(10,2)
AS
BEGIN
    IF EXISTS (SELECT 1 FROM Product WHERE ProductID = @ProductID)
    BEGIN
        UPDATE Product
        SET ProductName = @ProductName,
            Category = @Category,
            Price = @Price
        WHERE ProductID = @ProductID;
    END
    ELSE
    BEGIN
        INSERT INTO Product (ProductID, ProductName, Category, Price)
        VALUES (@ProductID, @ProductName, @Category, @Price);
    END
END;
