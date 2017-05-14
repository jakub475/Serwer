package Server;


import javax.rmi.CORBA.Tie;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Klasa reprezentująca użytkownika zapisanego w bazie
 */
public class User {

    /**
     * Nazwa użytkownika
     */
    private String name = null;
    /**
     * Hasło użytkownika
     */
    private String password = null;
    /**
     * Data ostatniego logowania
     */
    private Timestamp lastLogDate = null;

    /**
     * Konstruktor użytkownika
     * @param name nazwa użytkownika
     * @param password Hasło użytkownika
     * @param lastLogDate data ostatniego logowania
     */
    public User(String name, String password, Timestamp lastLogDate)
    {
        this.name = name;
        this.password = password;
        this.lastLogDate = lastLogDate;
    }

    /**
     * Funkcja zwracająca nazwę użytkownika
     * @return Nazwa użytkownika
     */
    public String getName()
    {
        return name;
    }

    /**
     * Funkcja zwracająca hasło użytkownika
     * @return Hasło użytkownika
     */
    public  String getPassword()
    {
        return password;
    }

    /**
     * Funkcja zwracająca datę ostatniego logowania
     * @return Data ostatniego logowania
     */
    public Timestamp getLastLogDate()
    {
        return lastLogDate;
    }

}