/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import netscape.javascript.JSException;
import org.json.JSONObject;

/**
 *
 * @author Kuba
 */

/**
 * Klasa serwera z zastosowanym wzorcem singleton, służy do akceptowania i autoryzacji nowych użytkowników podczas logowania
 */
public class Server implements Runnable{
    /**
     * Numer portu na którym będzie prowadzony nasłucj
     */
    private final int PORT = 8000;
    /**
     * Lista aktualnie zalogowanych użytkowników na serwerze
     */
    private ConcurrentHashMap<String, Client> clientList = new ConcurrentHashMap<>(); //lista aktualnie zalogowanych użytkowników
    /**
     * Lista kanałów znajdujących się na serwerze wraz z przypisanymi do nich nazwami użytkowników
     */
    private ConcurrentHashMap<String, ArrayList<String>> channelList = new ConcurrentHashMap<>();
    /**
     * Obiekt klasy Server
     */
    private static Server server = null;
    /**
     * Wykonawca wątków
     */
    private final ExecutorService clientProcessingPool = Executors.newFixedThreadPool(10);

    /**
     * Prywatny konstruktor klasy
     */
    private Server(){}

    /**
     * Funkcja zwracająca nazwy użytkowników  w danym kanale.
     * @return Lista użytkowników w kanale
     */
    synchronized public Set<String> getUsersName()
    {
        Set<String> keySet =  clientList.keySet();
        return keySet;
    }

    /**
     * Wyślij wiadomość do użytkownika o podanej nazwie
     * @param clientName Nazwa użytkownika do którego wyślemy wiadomość
     * @param message Wiadomość do wysłania
     * @return Prawda/Fałsz w zależności czy użytkownik był zalogowany i wiadomość do niego dotarła
     */
    synchronized public boolean Send(String clientName, JSONObject message)
    {
        if(clientList.containsKey(clientName))
        {
            clientList.get(clientName).Send(message);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Wysyła wiadomość do każdego zalogowanego użytkownika
     * @param message wiadomość któa zostanie wysłana
     */
    synchronized public void SendAll(JSONObject message){
        Collection<Client> collection =  clientList.values();
        for(Client col: collection)
        {
            col.Send(message);
        }
    }

    /**
     * Wyślij do każdego zalogowanego użytkownika oprócz podanego
     * @param message Wiadomość do wysłania
     * @param userName Nazwa użytkownika który zostanie pominięty
     */
    synchronized public void SendAllWithout(JSONObject message, String userName){
        ConcurrentHashMap<String, Client> tempClientList = new ConcurrentHashMap(clientList);
        tempClientList.remove(userName);
        Collection<Client> collection =  tempClientList.values();
        for(Client col: collection)
        {
            col.Send(message);
        }
    }

    /**
     * Funkcja usuwa użytkownika z listy aktualnie zalogowanych użytkowników.
     * @param userName Nazwa użytkownika
     */
    synchronized public void RemoveUser(String userName)
    {
        clientList.remove(userName);
    }

    /**
     * Uruchamia wątek serwera, pobierając jednocześnie z bazy danych listę istniejących kanałów oraz użytkowników w nich się znajdujących
     */
    public void startServer() {
        
        Thread serverThread = new Thread(this);
        serverThread.start();
        checkAndAddGeneral();
        loadDataFromBase();
    }

    /**
     * Funkcja zwracająca instancję serwera
     * @return Obiekt Server
     */
    static public Server getInstance()
    {
        if(server == null)
        {
            server = new Server();
            return server;
        }
        else
        {
            return server;
        }
    }

    /**
     * Funkcja służąca do wysłania zrzutu ekranu do całego kanału
     * @param channelName Nazwa kanłu
     * @param inputObject Obiekt reprezentujący otrzymany zrzut
     * @param clientName nazwa użytkownika nadającego
     * @param message Treść wiadomości
     */
    synchronized public void sendPhotoToChannel(String channelName, Object inputObject, String clientName, JSONObject message){
        for (String userName : channelList.get(channelName)) {
            if(!userName.equals(clientName) && clientList.containsKey(userName)) {
                Client client = clientList.get(userName);
                client.Send(message);
                client.sendPhoto( inputObject);
            }
        }
    }

    // Rzeczy do kanałów

    /**
     * Umożliwia dodanie nowego kanału do rozmów na serwerze
     * @param channelName
     * @param usersNames
     */
    synchronized public void addNewChannel(String channelName,ArrayList<String> usersNames)
    {
        channelList.put(channelName,usersNames);
    }

    /**
     * Funkcja dodająca użytkownika do kanału
     * @param channelName nawa kanału
     * @param userName nazwa użytkownika
     */
    synchronized public void addUserToChannel(String channelName, String userName){
        if(channelList.containsKey(channelName)){
            channelList.get(channelName).add(userName);
        }
    }

    /**
     * Pozwala wysłać wiadomość do wszystkich ludzi z danego kanału, oprócz osoby nadającej
     * @param channelName nazwa kanału
     * @param message wiadomość
     */
    synchronized public void sendToChannel(String channelName, JSONObject message)
    {

        if(message.has("from")) {
            for (String userName : channelList.get(channelName)) {
                if (!userName.equals(message.getString("from")))
                    Send(userName, message);
            }
        }
        else {
            for (String userName : channelList.get(channelName)) {
                Send(userName, message);
            }
        }

    }


    /**
     * Główna pętla programu odpowiedzialna za akceptowanie połączeń z nowymi użytkownikami
     */
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for clients to connect...");
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    Client client = new Client(clientSocket);
                    client.StartClient();
                    clientList.put(client.getClientName(), client);
                    clientProcessingPool.submit(client);

                }
                catch (IOException ioe) {
                    System.out.println("Nie można nawiązać połączenia");
                }
                catch (JSException JSe)
                {
                    System.out.println("Nieprawidłowy JSON");
                }
                catch (SQLException ex)
                {
                    Logger lgr = Logger.getLogger(DataBase.class.getName());
                    lgr.log(Level.WARNING, ex.getMessage(), ex);
                }
                catch (WrongLoginData wd)
                {
                    System.out.println("Niepoprawne dane logowania");
                }

            }
        } catch (IOException e) {
            System.err.println("Unable to process client request");
            e.printStackTrace();
        }
    }

    /**
     * Funkcja służąca do ładowanie danych z bazy danych. Pobiera listę aktywnych kanałów i przypisanych im użytkowników.
     */
    private void loadDataFromBase(){
        DataBase dataBase = new DataBase();
        try {
           dataBase.connect();
           ArrayList<String> channelNameArray = dataBase.getChannelArray();
           for(String channel : channelNameArray)
           {
               ArrayList<String> userInChannelArray = dataBase.getUserInChannel(channel);
               channelList.put(channel,userInChannelArray);
           }

        }
        catch (SQLException sql)
        {
            System.out.println("Nie sprawdzono czy general channel istnieje");
        }
        finally {
            dataBase.disconnect();
        }
    }

    /**
     * Funkcja sprawdza czy w bazie danych znajduje się kanał główny i ewentualnie go dodaje
     */
    private void checkAndAddGeneral()
    {
        DataBase dataBase = new DataBase();
        try {
            dataBase.connect();
            if(!dataBase.isUChannelInBase("general")) {
                dataBase.addChannel("general");
                System.out.println("Dodano kanał");
                Message mess = new Message("Server","general","Witaj w naszym czacie",new Timestamp(System.currentTimeMillis()),false,true);
                dataBase.addMessage(mess);
            }
        }
        catch (SQLException sql)
        {
            System.out.println("Nie sprawdzono czy general channel istnieje");
        }
        finally {
            dataBase.disconnect();
        }
    }

    /**
     * Funkcja zwracająca listę kanałów
     * @return Lista kanałów
     */
    public ConcurrentHashMap<String, ArrayList<String>> getChannelList() {
        return channelList;
    }
}
