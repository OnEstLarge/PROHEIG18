package util;

import java.sql.*;

public class DatabaseUtil {

    private static Connection connection;
    public void initConnection() throws SQLException {
        String url = "jdbc:mysql://127.0.0.1:3306/Odeep_Database?user=root&password=OdeepPRO2018";
        connection = DriverManager.getConnection(url);
    }

    /**
     * Ajoute un groupe dans la base de données s'il n'est pas déjà présent.
     *
     * @param groupID   nom du groupe
     * @return 0,   le groupe est déjà présent dans la base de données ou erreur
     *         1,   le groupe est ajouté à la base de donnée
     */
    public static int addGroupIfNotExists(String groupID) {
        String sql = "SELECT group_id FROM groups WHERE group_id = ?;";
        ResultSet resultSet = null;
        int result = 0;
        PreparedStatement preparedStatement    = null;
        PreparedStatement preparedStatementAdd = null;
        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, groupID);
            resultSet = preparedStatement.executeQuery();

            if(!resultSet.next()) {
                String sqlAdd = "INSERT INTO groups(group_id) VALUES(?);";
                preparedStatementAdd = connection.prepareStatement(sqlAdd);
                preparedStatementAdd.setString(1, groupID);
                preparedStatementAdd.executeUpdate();

                result = 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(preparedStatementAdd != null && preparedStatement != null) {
                    preparedStatementAdd.close();
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Ajoute un utilisateur dans la base de données s'il n'est pas déjà présent.
     *
     * @param username  nom d'utilisateur
     * @return 0,       l'utilisateur est déjà présent dans la base de données ou erreur
     *         1,       l'utilisateur est ajouté à la base de données
     */
    public static int addUserIfNotExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?;";
        ResultSet resultSet = null;
        int result = 0;
        PreparedStatement preparedStatement    = null;
        PreparedStatement preparedStatementAdd = null;

        try {
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            if(!resultSet.next()) {
                String sqlAdd = "INSERT INTO users(username) VALUES(?);";
                preparedStatementAdd = connection.prepareStatement(sqlAdd);
                preparedStatementAdd.setString(1, username);
                preparedStatementAdd.executeUpdate();

                result = 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(preparedStatementAdd != null && preparedStatement != null) {
                    preparedStatementAdd.close();
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
