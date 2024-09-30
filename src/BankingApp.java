import java.sql.*;
import java.util.Scanner;

public class BankingApp {
    private static final String ACCOUNT_TABLE_NAME = "bank_accounts";

    public static void main(String[] args) {
        try {
            createBankAccountsTableIfNotExists();

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n===== Banking Application =====");
                System.out.println("1. Create Account");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Check Balance");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        createAccount(scanner);
                        break;
                    case 2:
                        performTransaction(scanner, true);
                        break;
                    case 3:
                        performTransaction(scanner, false);
                        break;
                    case 4:
                        checkBalance(scanner);
                        break;
                    case 5:
                        System.out.println("Exiting the application. Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createBankAccountsTableIfNotExists() throws SQLException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = DatabaseUtil.getConnection();
            statement = connection.createStatement();
            String createTableQuery = "CREATE TABLE IF NOT EXISTS " + ACCOUNT_TABLE_NAME + " (" +
                    "account_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "account_number VARCHAR(20) NOT NULL UNIQUE, " +
                    "account_holder_name VARCHAR(100) NOT NULL, " +
                    "balance DECIMAL(10, 2) DEFAULT 0.00" +
                    ")";
            statement.executeUpdate(createTableQuery);
        } finally {
            DatabaseUtil.close(connection, statement, null);
        }
    }

    private static void createAccount(Scanner scanner) throws SQLException {
        System.out.print("Enter account holder's name: ");
        String accountHolderName = scanner.nextLine();
        String accountNumber = generateAccountNumber();
        double initialBalance = 0.0;

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DatabaseUtil.getConnection();
            String insertQuery = "INSERT INTO " + ACCOUNT_TABLE_NAME + " (account_number, account_holder_name, balance) VALUES (?, ?, ?)";
            preparedStatement = connection.prepareStatement(insertQuery);
            preparedStatement.setString(1, accountNumber);
            preparedStatement.setString(2, accountHolderName);
            preparedStatement.setDouble(3, initialBalance);

            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Account created successfully with account number: " + accountNumber);
            } else {
                System.out.println("Failed to create account. Please try again.");
            }
        } finally {
            DatabaseUtil.close(connection, preparedStatement, null);
        }
    }

    private static String generateAccountNumber() {

        return String.valueOf((int) (Math.random() * 1000000));
    }

    private static void performTransaction(Scanner scanner, boolean isDeposit) throws SQLException {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DatabaseUtil.getConnection();
            String selectQuery = "SELECT balance FROM " + ACCOUNT_TABLE_NAME + " WHERE account_number = ?";
            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, accountNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double currentBalance = resultSet.getDouble("balance");

                if (isDeposit) {
                    System.out.print("Enter deposit amount: ");
                } else {
                    System.out.print("Enter withdrawal amount: ");
                }

                double transactionAmount = scanner.nextDouble();
                scanner.nextLine();

                if (isDeposit) {
                    currentBalance += transactionAmount;
                } else {
                    if (currentBalance >= transactionAmount) {
                        currentBalance -= transactionAmount;
                    } else {
                        System.out.println("Insufficient balance for withdrawal.");
                        return;
                    }
                }

                String updateQuery = "UPDATE " + ACCOUNT_TABLE_NAME + " SET balance = ? WHERE account_number = ?";
                preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setDouble(1, currentBalance);
                preparedStatement.setString(2, accountNumber);
                int rowsUpdated = preparedStatement.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Transaction successful.");
                } else {
                    System.out.println("Transaction failed. Please try again.");
                }
            } else {
                System.out.println("Account not found. Please check the account number and try again.");
            }
        } finally {
            DatabaseUtil.close(connection, preparedStatement, null);
        }
    }

    private static void checkBalance(Scanner scanner) throws SQLException {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DatabaseUtil.getConnection();
            String selectQuery = "SELECT balance FROM " + ACCOUNT_TABLE_NAME + " WHERE account_number = ?";
            preparedStatement = connection.prepareStatement(selectQuery);
            preparedStatement.setString(1, accountNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                double balance = resultSet.getDouble("balance");
                System.out.println("Account Balance: $" + balance);
            } else {
                System.out.println("Account not found. Please check the account number and try again.");
            }
        } finally {
            DatabaseUtil.close(connection, preparedStatement, null);
        }
    }
}
