/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dsfinalproject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


/*
 * The Client with its GUI
 */
public class ClientGUI extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    // will first hold "Username:", later on "Enter message"
    private JLabel label;
    JLabel unamelbll;
    JLabel pwdlabel;
    // to hold the Username and later on the messages
    private JTextField tf;
   JPasswordField tpwd;
    // to hold the server address an the port number
    private JTextField tfServer, tfPort;
    // to Logout and get the list of the users
    private JButton login, logout, whoIsonline, register;
    // for the chat room
    private JTextArea ta;
    private JTextArea online;
    // if it is for connection
    private boolean connected;
    // the Client object
    private Client client;
    // the default port number
    private int defaultPort;
    private String defaultHost;
    private boolean onlineuser;
    JDialog d;

    // Constructor connection receiving a socket number
    ClientGUI(String host, int port) {

        super("Chat Client");
        defaultPort = port;
        defaultHost = host;

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(5, 1));
        // the server name anmd the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));
        // the two JTextField with default value for server address and port number
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);

        // the Label and the TextField for User Start Chat
        label = new JLabel("Enter your username and Password below", SwingConstants.CENTER);
        northPanel.add(label);
        unamelbll = new JLabel("Username", SwingConstants.RIGHT);
        northPanel.add(unamelbll);
        tf = new JTextField("");
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        pwdlabel = new JLabel("Password", SwingConstants.RIGHT);
        northPanel.add(pwdlabel);
        tpwd = new JPasswordField("");
        tpwd.setBackground(Color.WHITE);
        northPanel.add(tpwd);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which is the chat room
        ta = new JTextArea("Welcome to our Chat room\n", 40, 40);
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);

        online = new JTextArea("Online Clients on Chat room\n", 40, 40);
        online.setBackground(Color.GRAY);
        online.setForeground(Color.WHITE);
        centerPanel.add(online);
        online.setEditable(false);
        add(centerPanel, BorderLayout.CENTER);

        // the 4 buttons
        register = new JButton("Register");
        register.addActionListener(this);
        login = new JButton("Start Chat");
        login.addActionListener(this);
        logout = new JButton("Stop Chat");
        logout.addActionListener(this);
        logout.setEnabled(false);
        whoIsonline = new JButton("Who is Online");
        whoIsonline.addActionListener(this);
        whoIsonline.setEnabled(false);

        JPanel southPanel = new JPanel();
        southPanel.add(register);
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsonline);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 700);
        setVisible(true);
        tf.requestFocus();

    }

    // called by the Client to append text in the TextArea 
    void append(String str) {
        if (onlineuser && tf.getText().equals("")) {
            online.setText(str);
            online.setCaretPosition(online.getText().length() - 1);
           
        } else {
            //  ta.append(str);
            ta.setText(str);
            ta.setCaretPosition(ta.getText().length() - 1);
        }
 onlineuser = false;
    }
    // called by the GUI is the connection failed
    // we reset our buttons, label, textfield

    void connectionFailed() {
        login.setEnabled(true);
        register.setEnabled(true);
        tf.setText("");
        tpwd.setVisible(true);
        unamelbll.setVisible(true);
        pwdlabel.setVisible(true);
        label.setText("Enter User name and Password below");

        logout.setEnabled(false);
        whoIsonline.setEnabled(false);
        label.setText("Enter your username below");
        tf.setText("ACT");
        // reset port number and host name as a construction time
        tfPort.setText("" + defaultPort);
        tfServer.setText(defaultHost);
        // let the user change them
        tfServer.setEditable(false);
        tfPort.setEditable(false);
        // don't react to a <CR> after the username
        tf.removeActionListener(this);
        connected = false;
    }

    /*
	* Button or JTextField clicked
     */
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        // if it is the Logout button
        if (o == logout) {

            client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
            return;
        }
        // if it the who is in button
        if (o == whoIsonline) {
            onlineuser = true;
            client.sendMessage(new ChatMessage(ChatMessage.WHOISONLINE, ""));
            return;
        }

        // ok it is coming from the JTextField
        if (connected) {
            // just have to send the message
            client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, tf.getText()));
            tf.setText("");
            return;
        }

        if (o == login) {
            // ok it is a connection request
            String username = tf.getText().trim();
            // empty username ignore it
//            if (username.length() == 0) {
//                return;
//            }
            // empty serverAddress ignore it
            String server = tfServer.getText().trim();
            if (server.length() == 0) {
                return;
            }
            // empty or invalid port numer, ignore it
            String portNumber = tfPort.getText().trim();
            if (portNumber.length() == 0) {
                return;
            }
            int port = 0;
            try {
                port = Integer.parseInt(portNumber);
            } catch (Exception en) {
                return;   // nothing I can do if port number is not valid
            }
            //After check User Login from Database Here       
            // try creating a new Client with GUI
            String _pwd=tpwd.getText().trim();
            boolean _status = checkLogin(username, _pwd);
            if (_status) {
                client = new Client(server, port, username, this);

                // test if we can start the Client
                if (!client.start()) {
                    return;
                }

                tf.setText("");
                tpwd.setVisible(false);
                unamelbll.setVisible(false);
                pwdlabel.setVisible(false);
                label.setText("Enter your message below");
                connected = true;
                // disable register button
                register.setEnabled(false);
                // disable login button
                login.setEnabled(false);
                // enable the 2 buttons
                logout.setEnabled(true);
                whoIsonline.setEnabled(true);
                // disable the Server and Port JTextField
                tfServer.setEditable(false);
                tfPort.setEditable(false);
                // Action listener for when the user enter a message
                tf.addActionListener(this);
            }
        }
        if (o == register) {
            d = new JDialog();
            d.setSize(200, 200);
            JPanel jp = new JPanel(new GridLayout(6, 2, 1, 3));
            JLabel flabel = new JLabel("Full Name");
            JTextField fullname = new JTextField();
            JLabel unlabel = new JLabel("User Name");
            JTextField username = new JTextField();
            JLabel plabel = new JLabel("Password");
            JTextField pwd = new JTextField();
            JLabel cplabel = new JLabel("Confirm Password");
            JTextField cpwd = new JTextField();
            JButton saveUser = new JButton("Create");
            saveUser.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    User u = new User();
                    if (!pwd.getText().equals(cpwd.getText())) {
                        JDialog error = new JDialog();
                        JPanel p = new JPanel(new GridLayout(2, 1));
                        JLabel msg = new JLabel("Confirm Password is incorrect!!");
                        JButton ok = new JButton("Ok");
                        ok.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent ae) {
                                ok.setVisible(false);
                            }
                        });
                        p.add(msg);
                        p.add(ok);
                        error.add(p);
                        error.setSize(200, 200);
                        error.setVisible(true);
                    } else {
                        u.setFullname(fullname.getText());
                        u.setUsername(username.getText());
                        u.setPassword(pwd.getText());
                        save(u);
                    }
                }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    d.setVisible(false);
                }
            });
            jp.add(flabel);
            jp.add(fullname);
            jp.add(unlabel);
            jp.add(username);
            jp.add(plabel);
            jp.add(pwd);
            jp.add(cplabel);
            jp.add(cpwd);
            jp.add(saveUser);
            jp.add(cancel);
            d.add(jp);
            d.setVisible(true);
        }

    }

    // to start the whole thing the server
    public static void main(String[] args) {
        new ClientGUI("localhost", 1990);
    }

    public int save(User u) {
        int status = 0;
        System.setProperty("file.encoding", "UTF-8");
        try {
            Connection con = Conn.getConnection();
            
            PreparedStatement checkuname = con.prepareStatement("select * from users where username=?");
            checkuname.setString(1, u.getUsername());
            
            ResultSet rs = checkuname.executeQuery();
            
            if (rs.next()) {
                JDialog error = new JDialog();
                JPanel p = new JPanel(new GridLayout(2, 1));
                JLabel msg = new JLabel("User Name Already Exist!!");
                JButton ok = new JButton("Ok");
                ok.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        error.setVisible(false);
                    }
                });
                p.add(msg);
                p.add(ok);
                error.add(p);
                error.setSize(200, 200);
                error.setVisible(true);
            } else {
                PreparedStatement ps = con.prepareStatement(
                        "insert into users(fullname,username,password) values(?,?,?)");
                ps.executeQuery("SET CHARACTER SET 'UTF8'");
//insert into  users(fullname,username,password) values('aschalew','dgjgj','vnbvnb');
                ps.setString(1, u.getFullname());
                ps.setString(2, u.getUsername());
                ps.setString(3, u.getPassword());
                
                ps.executeQuery("SET CHARACTER SET 'UTF8'");
                status = ps.executeUpdate();
                
                
                if (status > 0) {
                    JDialog saved = new JDialog();
                    JPanel p = new JPanel(new GridLayout(2, 1));
                    JLabel msg = new JLabel("Account is Created!!");
                    JButton ok = new JButton("Ok");
                    ok.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            saved.setVisible(false);
                            d.setVisible(false);
                        }
                    });
                    p.add(msg);
                    p.add(ok);
                    saved.add(p);
                    saved.setSize(200, 200);
                    saved.setPreferredSize(new Dimension(300, 200));
                    saved.setVisible(true);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return status;
    }

    public boolean checkLogin(String username, String pwd) {
        boolean status = false;
        JDialog error = new JDialog();
        JPanel p = new JPanel(new GridLayout(2, 1));
        JLabel msg = new JLabel("");
        JButton ok = new JButton("Ok");
        if (username.equals("") || pwd.equals("")) {
            msg = new JLabel("User Name or Password can not be Empty!!");
            error.setPreferredSize(new Dimension(300, 200));
            p.add(msg);
            p.add(ok);
            error.add(p);
            error.setSize(200, 200);
            error.setVisible(true);
            return false;
        }
        try {
            Connection con = Conn.getConnection();
            PreparedStatement checkuname = con.prepareStatement("select * from users where username=?");
            checkuname.setString(1, username);
            ResultSet rs = checkuname.executeQuery();

            ok.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    error.setVisible(false);
                }
            });

            if (rs.next()) {
                if (!rs.getString("password").equals(pwd)) {
                    msg = new JLabel("Password Incorrect!!") ;
                    p.add(msg);
                    p.add(ok);
                    error.add(p);
                    error.setSize(200, 200);
                    error.setPreferredSize(new Dimension(300, 200));
                    error.setVisible(true);
                } else {
                    return true;
                }

            } else {
                msg = new JLabel("User Name Does not Exist!!");
                p.add(msg);
                p.add(ok);
                error.add(p);
                error.setSize(200, 200);
                error.setPreferredSize(new Dimension(300, 200));
                error.setVisible(true);
            }

        } catch (Exception e) {

        }
        return status;
    }
}
