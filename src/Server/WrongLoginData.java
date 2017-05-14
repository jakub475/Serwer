package Server;

/**
 * Created by Kuba on 01.12.2016.
 */

/**
 * Klasa błędu wykorzystywana przy odbierani wiadomości przez seerwer
 */
public class WrongLoginData extends Exception {

    /**
     * Funkcja zwracająca nazwę błędu
     * @return Treść błędu
     */
    public String getError()
    {
        return "Odebrane dane są niepoprawne";
    }
}
