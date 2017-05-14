package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Kuba
 */

/**
 * Klasa służąca do stworzenia oddzielnego wątku dla każdego z zalogowanych użytkowników
 */
public class Client implements Runnable{

    /**
     * Reprezentacja obiektu strumienia wyjścia
     */
    private PrintWriter out = null;
    /**
     * Reprezentacja bufora wejściowego
     */
    private BufferedReader in = null;
    /**
     * Gniazdo sieciowe klienta
     */
    private final Socket clientSocket;
    /**
     * Zmienna określająca czy pętla wątku głównego ma nadal działać
     */
    private boolean isWorking = true;
    /**
     * Nazwa użytkownika
     */
    private String clientName = null;
    /**
     * Hasło użytkownika
     */
    private String clientPassword = null;
    /**
     * Zmienna reprezentująca obiekt bazy danych
     */
    private DataBase base = new DataBase();
    /**
     * Strumień wyjściowy obiektów
     */
    private ObjectOutputStream outObject;
    /**
     * Strumień wejściowy obiektów
     */
    private ObjectInputStream inObject;
    /**
     * Socket odbioru obiektów
     */
    ServerSocket serverSocketObject;

    /**
     * Funkcja główna wykorzystywana do testów
     * @param args Jako argumenty przyjmuje argumenty podane w lini poleceń
     */
    public static void main(String[] args)
    {
        JSONObject message = new JSONObject();
        message.put("one","two");
        message.accumulate("one","three");
        JSONArray my = message.getJSONArray("one");

    }

    /**
     * Konstruktor klasy
     * @param clientSocket Socket klienta
     * @throws IOException
     */
    public Client(Socket clientSocket) throws IOException {
        System.out.println("Nowy użytkownik próbuje się zalogować");
        this.clientSocket = clientSocket;
      //  this.serverSocketObject = new ServerSocket(5000);
//        serverSocketObject = new ServerSocket(5000);
//        serverSocketObject.setSoTimeout(10000);
    }

    /**
     * Uwierzytelnianie użytkownika, funkcja pobiera nazwę użytkownika  jego hasło a następnie pyta bazę danych czy wszystko się zgadza
     * @throws IOException
     * @throws JSONException
     */
    public void StartClient() throws IOException, JSONException, SQLException, WrongLoginData
    {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//        outObject = new ObjectOutputStream(clientSocket.getOutputStream());
//        inObject = new ObjectInputStream(clientSocket.getInputStream());
        base.connect();

//        boolean isWorking = true;
        String inputLine;
        if (isWorking && (inputLine = in.readLine()) != null) {
           
                JSONObject message = new JSONObject(inputLine);
                System.out.println(message.toString());
                clientName = message.getString("name");
                clientPassword = message.getString("password");
                try{
                    User user = base.findUser(clientName);
                    if(user != null)    // Sprawdź czy użytkownik jest w bazie
                    {
                        if(clientName.equals(user.getName())&&clientPassword.equals(user.getPassword()))        // Sprawdź czy hasło i nazwa użytkownika zgadza się z tymi w bazie
                        {
                            System.out.println("Zalogowany użytkownik: " + clientName);
                            message = new JSONObject();
                            message.accumulate("Server","loggedIn");
                            message.accumulate("loginUser",clientName);
                            Send(message);
                            Server.getInstance().SendAll(message);
//                            isWorking = false;

                        }
                        else    //Jeśli nie zgadza się wyślij komunikat
                        {
                            message = new JSONObject();
                            message.accumulate("Server","unLogged");
                            message.accumulate("message","Niepoprawny login lub hasło");
                            Send(message);
                            System.out.println("Niepoprawne hasło dla użytkownika: " + clientName);
                            CloseConnection();
                            throw new WrongLoginData();
                        }
                    }
                    else // Jeśli użytkownika o danej nazwie nie ma stwórz go
                    {
                        user = new User(clientName,clientPassword,new Timestamp(System.currentTimeMillis()));
                        base.addUser(user);
                        System.out.println("Dodano użytkownika: " + clientName+" do bazy.");
                        message = new JSONObject();
                        message.accumulate("Server","loggedIn");
                        message.accumulate("loginUser",clientName);
                        Send(message);
                        message = new JSONObject();
                        message.accumulate("Server","newUser");
                        message.accumulate("UserName",clientName);
                        Server.getInstance().SendAll(message);

                        Server.getInstance().addUserToChannel("general",clientName);
                        base.addUserToChannel(clientName,"general");
                    }


                }
                catch(SQLException ex)
                {
                    Logger lgr = Logger.getLogger(DataBase.class.getName());
                    lgr.log(Level.SEVERE, ex.getMessage(), ex);
                }
        }
    }

    /**
     * Funkcja zwracająca nazwę użytkownika dla którego działa aktualny wątek
     * @return nazwa użytkownika
     */
    public String getClientName()
    {
        return clientName;
    }

    /**
     * Wysyła wiadomośc do użytkownika
     * @param message Obiekt JSON reprezentujący wysyłaną wiadomość
     */
    synchronized public void Send(JSONObject message)
    {
        out.println(message.toString());
    }

    /**
     * Funkcja wysyłająca zdjęcie do użytkownika będącego połączonego  z serwerem przy pomocy tego wątku;
     * @param inputPhoto Obiekt zdjęcia
     */
    synchronized public void sendPhoto(Object inputPhoto){
        try {

            serverSocketObject = new ServerSocket(5000);
            serverSocketObject.setSoTimeout(10000);
            System.out.println("Oczekiwanie na odebranie połączenia");
            Socket clientSocket = serverSocketObject.accept();

            outObject = new ObjectOutputStream(clientSocket.getOutputStream());
            outObject.writeObject(inputPhoto);
            outObject.close();
            serverSocketObject.close();
        }
        catch (IOException ioe){
            System.out.println("Przy przesyłaniu zdjęcia nie zadziałało IO");
        }
        finally {
            try {
                serverSocketObject.close();
            }
            catch (IOException ioe){
                System.out.println("Nie udało się zamknąć Socket'u");
            }

        }
        System.out.println("Zdjęcie wysłane");


    }

    /**
     * Funkcja pozwalająca na zamknięcie połączenia z użytkownikiem
     * @throws IOException
     */
    private void CloseConnection() throws IOException
    {
        out.close();
        in.close();
        clientSocket.close();
//        base.disconnect();
    }

    /**
     * Główna pętla wątku, czekająca na uzyskanie jakieś odpowiedzi od klienta i realizująca wymagane przez niego żądania
     */
    @Override
    public void run() {

       String inputLine;
        System.out.println("Odpalono wątek klienta");

        try {

            while ((inputLine = in.readLine()) != null && isWorking) {
            
                try {
                    JSONObject message = new JSONObject(inputLine);
                    System.out.println(message.toString());
                    if(message.has("Server"))  // Czy jest to polecenie do serwera
                    {
                        if(message.get("Server").equals("History")) // Polecenia klienta o zwrócenie historii danego kanału rozmowy
                        {
                            if(message.has("channelName")){     // Sprawdzenie czy zapytanie użytkownika odnosi się do kanału
                                ArrayList<Message > messageList = base.getChannelMessages(message.getString("channelName"));
                                for (Message oneMessage : messageList) {
                                    JSONObject jsonMessage = new JSONObject();
                                    jsonMessage.accumulate("Server", "History");
                                    jsonMessage.accumulate("from", oneMessage.getFrom());
                                    jsonMessage.accumulate("channelName", oneMessage.getTo());
                                    jsonMessage.accumulate("message", oneMessage.getMessage());
                                    jsonMessage.accumulate("date",oneMessage.getDate());
                                    Send(jsonMessage);
                                }
                            }
                            else{   // Jeśli jeśli jest to rozmowa bezpośrednia między użytkownikami
                                ArrayList<Message> messagesCollection = base.getMessages(message.getString("from"), message.getString("to"));
                                for (Message oneMessage : messagesCollection) {
                                    JSONObject jsonMessage = new JSONObject();
                                    jsonMessage.accumulate("Server", "History");
                                    jsonMessage.accumulate("from", oneMessage.getFrom());
                                    jsonMessage.accumulate("to", oneMessage.getTo());
                                    jsonMessage.accumulate("message", oneMessage.getMessage());
                                    jsonMessage.accumulate("date",oneMessage.getDate());
                                    Send(jsonMessage);
                                }
                            }
                        }
//                        else if(message.get("Server").equals("getUserList")) //Żądanie klienta o udostępnieni listy użytkowników zalogowanych na serwerze
//                        {
//                            JSONObject jsonmessage = new JSONObject();
//                            Set<String> usersName = Server.getInstance().getUsersName();
//                        }
                        else if(message.get("Server").equals("createNewChannel")) // Wysłanie informacji o stworzeniu nowego kanału rozmów
                        {
                           JSONArray users = message.getJSONArray("Users");
                           ArrayList<String> usersList = new ArrayList<>();
                           String nameOfChannel = message.getString("channelName");
                           for(Object user :users)
                           {
                                usersList.add((String)user);
                           }
                           Server.getInstance().addNewChannel(nameOfChannel,usersList);
                           base.addChannel(nameOfChannel,usersList);

                            message = new JSONObject();
                            message.put("Server", "channelName");
                            message.put("channelName",nameOfChannel );
                            for(String userName :usersList){
                                message.accumulate("userNames",userName);
                            }
                            Server.getInstance().sendToChannel(nameOfChannel,message);

                            JSONObject jsonMessage = new JSONObject();
                            jsonMessage.accumulate("Server", "History");
                            jsonMessage.accumulate("from", "Serwer");
                            jsonMessage.accumulate("channelName", nameOfChannel);
                            jsonMessage.accumulate("message", "Zostałeś zaproszony do rozmowy w tym kanale");
                            jsonMessage.accumulate("date",new Timestamp(System.currentTimeMillis()));
                            //Server.getInstance().sendToChannel(nameOfChannel,jsonMessage);
                            //Send(message);

                            //Server.getInstance().sendToChannel(message.getString("channelName"),message);
                            if (base.isUChannelInBase(nameOfChannel)) {
                                Timestamp date = new Timestamp(System.currentTimeMillis());
                                base.addMessage(new Message(jsonMessage.getString("from"), jsonMessage.getString("channelName"), jsonMessage.getString("message"), date, false,true));
                            }

                        }
                        else if (message.get("Server").equals("getChannelNames")) // Wysłanie udostępnionej listy kanałów listy aktywnych kanałów
                        {
                            Enumeration<String> channelList = Server.getInstance().getChannelList().keys();

                            while(channelList.hasMoreElements()){
                                String channelName = channelList.nextElement();
                                message = new JSONObject();
                                message.put("Server", "channelName");
                                message.put("channelName",channelName );
                                ArrayList<String> usersNames = Server.getInstance().getChannelList().get(channelName);
                                if(!usersNames.contains(clientName))
                                    continue;
                                if (usersNames.isEmpty())
                                    continue;
                                for(String userName :usersNames){
                                    message.accumulate("userNames",userName);
                                }
                                Send(message);
                            }
                        }
                        else if(message.get("Server").equals("getLoggedUsers")){  // Zwrócenie listy zalogowanych użytkowników
                            message = new JSONObject();
                            message.accumulate("Server","loggedUsers");
                            int i =0;
                            for(String oneName: Server.getInstance().getUsersName())
                            {
                                ++i;
                                message.accumulate("user"+Integer.toString(i),oneName);
                            }
//                            message.accumulate("LoggedUsers",Integer.toString(i));
                            System.out.println(message.toString());
                            Send(message);
                        }
                        else if(message.get("Server").equals("sendPhoto")){ // Powiadomienie o nadchodzącym zdjęciu

                            serverSocketObject = new ServerSocket(5000);
                            serverSocketObject.setSoTimeout(10000);
                            System.out.println("Oczekiwanie na odebranie połączenia");
                            Socket clientSocket = serverSocketObject.accept();
                            inObject = new ObjectInputStream(clientSocket.getInputStream());
                            Object inputObject = inObject.readObject();

                            //Server.getInstance().sendToChannel(message.getString("channelName"),message);
                            inObject.close();
                            serverSocketObject.close();
                            System.out.println("Odebrano zdjęcie");
                            Server.getInstance().sendPhotoToChannel(message.getString("channelName"),inputObject, clientName,message);

                        }
                        else if(message.get("Server").equals("close")){
                            isWorking = false;
                        }
                    }
                    else if(message.has("from")&&message.has("to"))  // Wiadomość bezpośrednio do drugiego użytkownika
                    {

                        Server.getInstance().Send(message.getString("to"), message);

                        if (base.isUserInBase(message.getString("to"))) {
                            Timestamp date = new Timestamp(System.currentTimeMillis());
                            base.addMessage(new Message(message.getString("from"), message.getString("to"), message.getString("message"), date, false,false));
                        }


                    }
                    else  if(message.has("from")&& message.has("channelName")) // Wiadomość pochodzi od kanału
                    {
                        Server.getInstance().sendToChannel(message.getString("channelName"),message);
                        if (base.isUChannelInBase(message.getString("channelName"))) {
                            Timestamp date = new Timestamp(System.currentTimeMillis());
                            base.addMessage(new Message(message.getString("from"), message.getString("channelName"), message.getString("message"), date, false,true));
                        }

                    }

                } catch (JSONException JSONe) {
                    System.err.println("Paczka która przyszła jest niepoprawna");
                }
                catch (SQLException sqe)
                {
                    Logger lgr = Logger.getLogger(DataBase.class.getName());
                    lgr.log(Level.SEVERE, sqe.getMessage(), sqe);
                    System.out.println("Nie zapisano wiadomości w bazie");
                }catch (ClassNotFoundException cnfe){
                    System.out.println("Otrzymany obiekt nie należy do żadnej znanej klasy");
                }

        }
            
        } catch (IOException ioe) {
            System.out.println("Połączenie zostało utracone");
            ioe.printStackTrace();
        } finally {
            try {
                Server.getInstance().RemoveUser(clientName);
                CloseConnection();
                System.out.println("Zamknięto socket i bufory");
                base.setLastLogDate(clientName);
                base.disconnect();

                // wyślij powiadomienie pozostałym że użytkownika nie ma już z nami
                JSONObject jsonmessage = new JSONObject();
                jsonmessage.accumulate("Server","loggedOut");
                jsonmessage.accumulate("loginUser",clientName);
                Server.getInstance().SendAll(jsonmessage);

            } catch (IOException iOException) {
                System.out.println("Nie zamknięto socketu");
            }
            catch(SQLException ex){
                Logger lgr = Logger.getLogger(DataBase.class.getName());
                lgr.log(Level.SEVERE, ex.getMessage(), ex);
            }

        }
        
        System.out.println("Koniec");

    }

}
