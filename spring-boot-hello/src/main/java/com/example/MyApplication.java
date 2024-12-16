package com.example;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
@SpringBootApplication
public class MyApplication {

	@RequestMapping("/")
	String home() throws SQLException {
    String jdbcUrl = "jdbc:postgresql://localhost:5432/postgres";
    String username = "rohit.chavan";
    String password = "postgres";

    // Register the PostgreSQL driver

    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Connect to the database
    Connection connection = null;
    try {
      connection = DriverManager.getConnection(jdbcUrl, username, password);

      Statement statement = connection.createStatement();

      ResultSet resultSet = statement.executeQuery("SELECT * FROM cars");

      while (resultSet.next())
      {
        String columnValue = resultSet.getString("color");
        System.out.println("Column Value: " + columnValue);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // Perform desired database operations

    // Close the connection
    connection.close();

		return "Hello World!";
	}

	public static void main(String[] args) {
		SpringApplication.run(MyApplication.class, args);
	}

}