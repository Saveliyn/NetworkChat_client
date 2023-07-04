package lesson7.server.services;

import java.sql.SQLException;

public interface AuthenticationService {
    String getUsernameByLoginAndPassword(String login, String password) throws SQLException, ClassNotFoundException;
}
