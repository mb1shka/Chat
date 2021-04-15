package com.javarush.task.task30.task3008;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleHelper {
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void writeMessage(String message) {  //выводит переданное сообщение на экран
        System.out.println(message);
    }

    public static String readString() {  //считывает строку с консоли.
        String line;
        try {
            line = reader.readLine();
            return line;
        } catch (IOException e) {
            writeMessage("Произошла ошибка при попытке ввода текста. Попробуйте еще раз.");
            return line = readString();
        }
    }

    public static int readInt() {  //возвращает введенное число
        int num;
        try {
            num = Integer.parseInt(readString());
            return num;
        } catch (NumberFormatException e) {
            writeMessage("Произошла ошибка при попытке ввода числа. Попробуйте еще раз.");
            return num = readInt();
        }
    }
}
