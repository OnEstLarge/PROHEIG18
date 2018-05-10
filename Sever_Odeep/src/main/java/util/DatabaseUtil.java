package util;

import java.sql.*;

public class DatabaseUtil {

    private static Connection connection;

    private void initConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/sakila?user=root&password=OdeepPRO2018";
        connection = DriverManager.getConnection(url);
    }

    /**
     * Ajoute un groupe dans la base de données s'il n'est pas déjà présent.
     *
     * @param groupID   nom du groupe
     * @return 0,   le groupe est déjà présent dans la base de données ou erreur
     *         1,   le groupe est ajouté à la base de donnée
     * @throws SQLException
     */
    private static int addGroupIfNotExists(String groupID) throws SQLException {
        String sql = "SELECT group_id FROM groups WHERE group_id = ?)";
        ResultSet resultSet = null;
        int result = 0;
        PreparedStatement preparedStatement    = null;
        PreparedStatement preparedStatementAdd = null;
        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, groupID);
            resultSet = preparedStatement.executeQuery();

            if(!resultSet.next()) {
                String sqlAdd = "insert into groups(group_id) values(?)";
                preparedStatementAdd = connection.prepareStatement(sqlAdd);
                preparedStatementAdd.setString(1, groupID);
                preparedStatementAdd.executeUpdate();

                result = 1;
            }

        } finally {
            preparedStatementAdd.close();
            preparedStatement.close();
        }

        return result;
    }

    public static void uploadJSON(String filenameJSON, String group){

    }

    public static String downloadJSON(String group){

        return null;
    }

}
