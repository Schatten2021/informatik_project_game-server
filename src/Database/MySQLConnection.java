package Database;

import java.sql.*;
import java.sql.Connection;

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
        int rowCount = queryResult.getMetaData().getColumnCount();

        String[] columnNames = new String[columnCount];
        String[] columnTypes = new String[columnCount];

        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = queryResult.getMetaData().getColumnName(i + 1);
            columnTypes[i] = queryResult.getMetaData().getColumnTypeName(i + 1);
        }

        String[][] rowData = new String[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++) {
            queryResult.next();
            for (int j = 0; j < columnCount; j++) {
                rowData[i][j] = queryResult.getString(j + 1);
            }
        }

        return new QueryResult(rowData, columnNames, columnTypes);
    }
    public void close() throws SQLException {
        this.connection.close();
    }
}
