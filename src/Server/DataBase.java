/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;


import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Kuba
 */

/**
 * Klasa służąca do połączenia z bazą danych i wymiany informacji z nią.
 */
public class DataBase {


    /**
     * Zmienna reprezentująca połączenie z bazą danych
     */
    private Connection con = null;

    /**
     * Zmienna służąca do egzekwowania poleceń MySQL
     */
    private Statement st = null;
    /**
     * Zmienna służąca do obierania wyników zwracanych przez bazę MySQL.
     */
    private ResultSet rs = null;
    /**
     * Obiekt reprezentujący stan zapytania MySQL przed wysłaniem
     */
    private PreparedStatement preparedStatement = null;

    /**
     * Adres pod którym działa baza mySQL
     */
    private String url = "jdbc:mysql://localhost:3306/chat?useUnicode=true&characterEncoding=UTF-8";
    /**
     * Nazwa użytkownika do zalogowania się w bazie
     */
    private String user = "testuser";
    /**
     * Hasło do zalogowania się w bazie
     */
    private String password = "testuser";

    /**
     * Funkcja służąca do testów klasy
     * @param args brak zastosowania
     */
    public static void main(String[] args) {

        DataBase bazaDanych = new DataBase();
        try {
            bazaDanych.connect();
            Timestamp data = new Timestamp(System.currentTimeMillis());
            User Kuba = new User("Kuba","haslo",data);
            Message wiadomosc = new Message("Kuba","general","Wiadomosc",new Timestamp(System.currentTimeMillis()),false, true);
            bazaDanych.addMessage(wiadomosc);
            User foundUser = bazaDanych.findUser("123");
            bazaDanych.setLastLogDate("Kuba");

            ArrayList<String> list2 = bazaDanych.getUserInChannel("ludzieZRok3u3");
            for(String one :list2)
            {
                System.out.println(one);
            }

            ArrayList<String> list = new ArrayList<>();
            list.add("one");
            list.add("two");
            if(foundUser != null)
            {
                System.out.println(foundUser.getName());
                System.out.println(foundUser.getPassword());
                System.out.println(foundUser.getLastLogDate().toString());
            }
        }
        catch(SQLException ex)
        {
            Logger lgr = Logger.getLogger(DataBase.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }
        finally {
            bazaDanych.disconnect();

        }


    }

    /**
     * Konstruktor klasy DateBase
     */
    public DataBase() { }

    /**
     * Funkcja służąca do połączenia z bazą danych SQL
     */
    public void connect() throws SQLException
    {
        con = DriverManager.getConnection(url, user, password);
        st = con.createStatement();
        rs = st.executeQuery("SELECT VERSION()");
        //cStmt = con.prepareCall("{call demoSp(?, ?)}");
        if (rs.next()) {
            System.out.println(rs.getString(1));
        }
    }

    /**
     * Zamknij połączenie z bazą danych
     */
    public void disconnect()
    {
        try {

            if (rs != null) {
                rs.close();
            }

            if (st != null) {
                st.close();
            }

            if (con != null) {
                con.close();
            }

        } catch (SQLException ex) {

            Logger lgr = Logger.getLogger(DataBase.class.getName());
            lgr.log(Level.WARNING, ex.getMessage(), ex);
        }
    }


    /**
     * Znajduje użytkownika w bazie danych i zwraca obiekt z danymi użytkownika
     * @param userName nazwa użytkownika
     * @return zwraca obiekt klasy User
     * @throws SQLException
     */
    public User findUser(String userName) throws SQLException
    {

        rs = st.executeQuery("SELECT * FROM chat.users WHERE name=\""+userName+"\"");
        if(rs == null)
            return null;
        String name = null;
        String password = null;
        Timestamp lastLogDate = null;

        if(!rs.next())      // jeżeli nie ma odpowiedzi to znaczy że użytkownika nie znaleziono
        {
            System.out.println("Nie ma usera");
            return null;
        }
        else
        {
            name=rs.getString("name");
            password = rs.getString("password");
            lastLogDate = rs.getTimestamp("log_date");
        }

        User user = new User(name,password,lastLogDate);
        return user;
    }

    /**
     * Funkcja sprawdzająca czy użytkownik jest w bazie danych
     * @param userName nazawa użytkownika
     * @return Prawda/Fałsz użytkownik jest w bazie
     * @throws SQLException
     */
    public boolean isUserInBase(String userName)throws SQLException
    {
        rs = st.executeQuery("SELECT * FROM chat.users WHERE name=\""+userName+"\"");
        if(rs == null)
            return false;

        if(!rs.next())      // jeżeli nie ma odpowiedzi to znaczy że użytkownika nie znaleziono
        {
            System.out.println("Nie ma użytkownika");
            return false;
        }
        else
        {
            if(userName.equals(rs.getString("name")))
            {
                return true;
            }
            else
                return false;

        }
    }

    /**
     * Funkcja sprawdzająca czy dany kanał znajduje się w bazie
     * @param chatName nazwa kanału
     * @return watrość logiczna obecności kanału w bazie
     * @throws SQLException
     */
    public boolean isUChannelInBase(String chatName)throws SQLException
    {
        rs = st.executeQuery("SELECT * FROM chat.chatname WHERE chatname=\""+chatName+"\"");
        if(rs == null)
            return false;

        if(!rs.next())      // jeżeli nie ma odpowiedzi to znaczy że użytkownika nie znaleziono
        {
            System.out.println("Nie ma takiego kanału");
            return false;
        }
        else
        {
            if(chatName.equals(rs.getString("chatName")))
            {
                return true;
            }
            else
                return false;

        }
    }

    /**
     * Funkcja ustawia ostatni czas zalogowania użytkownika na aktualny czas serwera
     * @param userName nazwa użytkownika
     */
    public void setLastLogDate(String userName) throws SQLException
    {
        preparedStatement = con.prepareStatement("UPDATE chat.users SET log_date = ? WHERE name = \""+userName+"\";");
        preparedStatement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        System.out.println(preparedStatement.toString());
        preparedStatement.executeUpdate();
    }

    /**
     * Dodanie użytkownika do bazy danych
     * @param user Obiekt reprezentujący użytkownika
     * @throws SQLException
     */
    public void addUser(User user) throws SQLException {

        preparedStatement = con.prepareStatement("insert into  chat.users values (?, ?, ?)");
        // "myuser, webpage, datum, summary, COMMENTS from feedback.comments");
        // Parameters start with 1
        preparedStatement.setString(1, user.getName());
        preparedStatement.setString(2, user.getPassword());
        preparedStatement.setTimestamp(3, user.getLastLogDate());
        preparedStatement.executeUpdate();

    }

    /**
     * Funkcja dodająca nową wiadomość do bazy danych
     * @param message treść wiadomości
     * @throws SQLException
     */
    public void addMessage(Message message)throws SQLException
    {
        preparedStatement = con.prepareStatement("insert into  chat.messages values (?, ?, ?,?, ?,?)");
        if(message.isChannel()){
            preparedStatement.setString(1, message.getFrom());
            preparedStatement.setString(2, "");
            preparedStatement.setString(3, message.getTo());
            preparedStatement.setString(4, message.getMessage());
            preparedStatement.setTimestamp(5, message.getDate());
            preparedStatement.setBoolean(6, message.getRead());
        }
        else {
            preparedStatement.setString(1, message.getFrom());
            preparedStatement.setString(2, message.getTo());
            preparedStatement.setString(3,"" );
            preparedStatement.setString(4, message.getMessage());
            preparedStatement.setTimestamp(5, message.getDate());
            preparedStatement.setBoolean(6, message.getRead());
        }
        preparedStatement.executeUpdate();
    }

    /**
     * Funkcja zwracająca wiadomości zapisane w bazie od 'form' do 'to'
     * @param from nazwa użytkownika który nadał wiadomości
     * @param to nazwa użytkownika który odebrał wiadomość
     * @return Lista wiadomości pomiędzy użytkownikami
     * @throws SQLException
     */
    public  ArrayList<Message> getMessages(String from, String to) throws SQLException
    {
        rs = st.executeQuery(" select * FROM chat.messages where `from` =\""+from+"\" AND `to` =\""+to +"\" OR `to` =\""+from+"\" AND `from` =\""+to+"\" ORDER BY send_date ASC;");

        ArrayList<Message> messages = new ArrayList<>();
        String mfrom = null;
        String mto = null;
        String mMessage = null;
        Timestamp mSendDate = null;
        Boolean modczytano = null;
        while (rs.next())
        {
            mfrom=rs.getString("from");
            mto = rs.getString("to");
            mMessage = rs.getString("message");
            mSendDate = rs.getTimestamp("send_date");
            modczytano = rs.getBoolean("odczytano");
            messages.add(new Message(mfrom,mto, mMessage,mSendDate,modczytano, false));
        }

        return messages;

    }

    // obsługa kanałów

    /**
     * Dodanie nowego kanału do bazy danych
     * @param channelName nawa kanału który należy dodać
     * @param usersNames Lista użytkowników przypisanych do kanału
     * @throws SQLException
     */
    public void addChannel(String channelName, ArrayList<String> usersNames) throws SQLException{
        preparedStatement = con.prepareStatement("insert into  chat.chatname values (?)");
        preparedStatement.setString(1, channelName);
        preparedStatement.executeUpdate();

        for(String user :usersNames) {
            preparedStatement = con.prepareStatement("insert into  chat.userslistchat values (?,?)");
            preparedStatement.setString(1, user);
            preparedStatement.setString(2, channelName);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Funkcja dodająca kanał do bazy danych bez przypisywania mu żadnych użytkowników
     * @param channelName nazwa kanału
     * @throws SQLException
     */
    public void addChannel(String channelName) throws SQLException{
        preparedStatement = con.prepareStatement("insert into  chat.chatname values (?)");
        preparedStatement.setString(1, channelName);
        preparedStatement.executeUpdate();
    }

    /**
     * Funkcja zwracająca listę użytkowników znajdujących się w danym kanale zapisanym w bazie danych
     * @param channelName nazwa kanału którego użytkowników ma zwrócić funkcja
     * @return lista użytkowników zapisanych w danym kanale
     * @throws SQLException
     */
    public ArrayList<String> getUserInChannel(String channelName)throws SQLException{
        rs = st.executeQuery(" select * FROM chat.userslistchat where `chatname` =\""+channelName+"\";");
        ArrayList<String> userList = new ArrayList<>();
        String userName;
        while (rs.next())
        {
            userName = rs.getString("userName");
            userList.add(userName);
        }
        return userList;
    }

    /**
     * Funkcja zwraca listę kanałów zapisanych w bazie danych
     * @return zwraca listę kanałów zapisanych w bazie
     * @throws SQLException
     */
    public ArrayList<String> getChannelArray()throws SQLException{
        rs = st.executeQuery(" select * FROM chat.chatname ;");
        ArrayList<String> chatNameArray = new ArrayList<>();
        String chatName;
        while (rs.next())
        {
            chatName = rs.getString("chatName");
            chatNameArray.add(chatName);
        }
        return chatNameArray;
    }

    /**
     * Dodaje użytkownika do bazy
     * @param userName nazwa użytkownika dodawanego do bazy
     * @param channelName nazwa kanału do którego chcemy dodać użytkownika
     * @throws SQLException
     */
    public void addUserToChannel(String userName, String channelName)throws SQLException{
        preparedStatement = con.prepareStatement("insert into  chat.userslistchat values (?,?)");
        preparedStatement.setString(1, userName);
        preparedStatement.setString(2, channelName);
        preparedStatement.executeUpdate();
    }

    /**
     * Funkcja zwracająca listę wiadomości znajdujących się w danym kanale
     * @param channelName nazwa kanału
     * @return Lista wiadomości znajdujących się w kanale
     * @throws SQLException
     */
    public ArrayList<Message> getChannelMessages(String channelName) throws SQLException {

        rs = st.executeQuery(" select * FROM chat.messages where  `channelname` =\""+ channelName +"\" ORDER BY send_date ASC;");

        ArrayList<Message> messages = new ArrayList<>();
        String mfrom = null;
        String mto = null;
        String mMessage = null;
        Timestamp mSendDate = null;
        Boolean modczytano = null;
        while (rs.next())
        {
            mfrom=rs.getString("from");
            mto = rs.getString("channelName");
            mMessage = rs.getString("message");
            mSendDate = rs.getTimestamp("send_date");
            modczytano = rs.getBoolean("odczytano");
            messages.add(new Message(mfrom,mto, mMessage,mSendDate,modczytano, true));
        }

        return messages;
    }





}
