/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dsfinalproject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author user1
 */
public class Conn {
    
    public static Connection getConnection() throws ClassNotFoundException {
        Connection con = null;
        String db = "dsfinal";
        String usr = "root";
        try {

            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + db+"?useUnicode=yes&characterEncoding=UTF-8", usr, "");
            
            //out.print("Database Connected");
        } catch (SQLException e) {
            System.out.println(e);
        }

        return con;
    }
}
