/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

/**
 *
 * @author Kuba
 */

/**
 * Klasa inicjująca uruchomienie programu
 */
public class Main {

    /**
     * Funkcja inicjacyjna
     * @param args
     */
    public static void main(String[] args) {
            Server server = Server.getInstance();
            server.startServer();
    }
    
}
