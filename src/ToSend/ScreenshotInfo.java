package ToSend;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa służąca do wysyłania zrzutów ekranów pomiędzy użytkownikami
 */
public class ScreenshotInfo implements Serializable{

    /**
     * Zmienna reprezentująca zrzut ekranu
     */
    private ImageIcon screenshot;
    /**
     * Zmienna reprezentująca narysowane punkty na ekranie
     */
    private ArrayList<Point> points;

    /**
     * Konstruktor klasy
     * @param img Obrazek ze zrzutem ekranu
     * @param pts Lista punktów wykorzystywanych do rysunku
     */
    public ScreenshotInfo(BufferedImage img, ArrayList<Point> pts) {
        this.screenshot = new ImageIcon(img);
        this.points = pts;
    }

    /**
     * Funkcja zwracająca zrzut ekranu
     * @return Zrzut ekranu
     */
    public ImageIcon getScreenshot() {
        return this.screenshot;
    }

    /**
     * Funkcja zwracająca listę punktów użytych przy rysunku
     * @return Lista punktów
     */
    public ArrayList<Point> getPointsArray() {
        return this.points;
    }
}