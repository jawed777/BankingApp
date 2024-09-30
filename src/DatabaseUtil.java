import java.sql.*;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bank_data";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
