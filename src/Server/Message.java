package Server;


import java.sql.Timestamp;

/**
 * Created by Kuba on 30.11.2016.
 */

/**
 * Klasa opisująca pojedynczą wiadomość nadawaną przez użytkownika
 */
public class Message{
    /**
     * Nazwa nadawcy
     */
    private String from;
    /**
     * Nazwa odbiorcy
     */
    private String to = "";
    /**
     * Treść wiadomości
     */
    private String message;
    /**
     * Data nadania wiadomości
     */
    private Timestamp date;
    /**
     * Znacznik odczytania wiadomości
     */
    private boolean read;
    /**
     * Znacznik obecności wiadomości w kanale
     */
    private boolean isChannel;

    /**
     * Konstruktor klasy
     * @param from Nazwa użytkownika nadającego wiadomość
     * @param to Nazwa użytkownika obierającego wiadomość
     * @param message Treść wiadomości
     * @param date Data nadania wiadomości
     * @param read Znacznik przeczytania wiadomości
     * @param isChannel Zmienna oznaczająca czy wiadomość należy do kanału, czy została nadana pomiędzy użytkownikami
     */
    public Message(String from, String to, String message, Timestamp date, boolean read, boolean isChannel)
    {
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
        this.read = read;
        this.isChannel = isChannel;
    }

    /**
     * Funkcja zwracająca nazwę nadawcy wiadomości
     * @return nazwa nadawcy
     */
    public String getFrom()
    {
        return from;
    }

    /**
     * Funkcja zwracająca nazwę odbiorcy wiadomości
     * @return nazwa odbiorcy
     */
    public String getTo()
    {
        return to;
    }

    /**
     * Funkcja zwracająca treść wiadomości
     * @return treść wiadomości
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * Funkcja zwracająca datę nadania wiadomości
     * @return Data nadani
     */
    public Timestamp getDate()
    {
        return date;
    }

    /**
     * Funkcja zwracająca informację o stanie pola oznaczające przeczytanie wiadomości przez użytkownika
     * @return Zwraca stan przeczytania wiadomości przez użytkownika
     */
    public boolean getRead()
    {
        return read;
    }

    /**
     * Funckja zwraca wartość logiczną informującą czy wiadomość jest wiadomością wysyłaną przez kanał czy przez użytkownika
     * @return Wartość logiczna określająca czy wiadomość jest w kanale
     */
    public boolean isChannel() {
        return isChannel;
    }
}