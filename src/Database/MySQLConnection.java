package Database;

import Abitur.Queue;

import java.sql.*;
import java.util.HashMap;

public class MySQLConnection {
    private final Connection connection;
    private final HashMap<String, PreparedStatement> preparedStatements = new HashMap<>();

    public MySQLConnection(String host, int port, String database, String username, String password) throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s", host, port, database);
        this.connection = DriverManager.getConnection(url, username, password);
    }
    public void prepareStatement(String name, String sql) throws SQLException {
        this.preparedStatements.put(name, connection.prepareStatement(sql));
    }
    public QueryResult runPreparedStatement(String name, Object ... values) throws SQLException {
        if (!this.preparedStatements.containsKey(name)) {
            throw new IllegalArgumentException("No such prepared statement: " + name);
        }
        PreparedStatement statement = this.preparedStatements.get(name);
        for (int i = 0; i < values.length; i++) {
            statement.setObject(i + 1, values[i]);
        }
        if (!statement.execute()) {
            statement.clearParameters();
            return null;
        }
        ResultSet queryResult = statement.getResultSet();
        statement.clearParameters();

        int columnCount = queryResult.getMetaData().getColumnCount();

        int rowCount = 0;
        Queue<String[]> results = new Queue<>();
        while (queryResult.next()) {
            rowCount++;
            String[] row = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = queryResult.getString(i + 1);
            }
            results.enqueue(row);
        }

        String[] columnNames = new String[columnCount];
        String[] columnTypes = new String[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = queryResult.getMetaData().getColumnName(i + 1);
            columnTypes[i] = queryResult.getMetaData().getColumnTypeName(i + 1);
        }

        String[][] rowData = new String[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++) {
            rowData[i] = results.front();
            results.dequeue();
        }

        return new QueryResult(rowData, columnNames, columnTypes);
    }
    public void close() throws SQLException {
        this.connection.close();
    }
}
