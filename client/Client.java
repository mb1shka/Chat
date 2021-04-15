package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.Connection;
import com.javarush.task.task30.task3008.ConsoleHelper;
import com.javarush.task.task30.task3008.Message;
import com.javarush.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {

    protected Connection connection;
    private volatile boolean clientConnected = false;

    protected String getServerAddress() {
        //запрашивает ввод адреса сервера у пользователя
        ConsoleHelper.writeMessage("Введите адрес сервера:");

        //возвращает введенное значение
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        //запрашивает ввод порта сервера
        ConsoleHelper.writeMessage("Введите порт сервера:");

        //возвращает введенное значение
        return ConsoleHelper.readInt();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
        //метод всегда возвращает true, так как клиент всегда пишет в консоль
        //но мы можем переопределить метод в другом клиенте, который не должен отправлять текст в консоль
    }

    protected SocketThread getSocketThread() {
        //создает и возвращает новый объект класса SocketThread
        SocketThread socketThread = new SocketThread();
        return socketThread;
    }

    protected String getUserName() {
        //запрашивает имя пользователя
        ConsoleHelper.writeMessage("Введите имя пользователя:");

        //возвращает имя пользователя
        return ConsoleHelper.readString();
    }

    protected void sendTextMessage(String text) {
        //создает новое текстовое сообщение, используя переданный текст, затем
        //отправляет его серверу
        try {
            Message message = new Message(MessageType.TEXT, text);
            connection.send(message);
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Не удалось отправить сообщение.");
            clientConnected = false;
        }
    }

    public void run() {
        SocketThread socketThread = getSocketThread();
        //создание нового сокетного потока

        socketThread.setDaemon(true);
        socketThread.start();
        //делаем его демоном (так как он служебный) и запускаем поток

        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException e) {
            ConsoleHelper.writeMessage("Ожидание потока прервано.");
            return;
        }
        //поток ждет, пока он не получит нотификацию из другого объекта

        if (clientConnected) {
            ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
        } else {
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
        }

        while (true) {
            if (clientConnected) {
                String line = ConsoleHelper.readString();
                //читаем сообщение с консоли, пока клиент подключен

                if (line.equals("exit")) break;
                //если клиент написал "exit", выходим из цикла
                if (shouldSendTextFromConsole()) sendTextMessage(line);
                //если нет, проверяем, клиент ли это вообще написал, и если да, то отправляем сообщение в чат
            }
        }
    }

    public static void main(String[] args) {
        Client client =  new Client();
        client.run();
    }

    public class SocketThread extends Thread {
        //класс отвечает за поток, устанавливающий сокетное соединение и читающий сообщения сервера

        protected void processIncomingMessage(String message) {
            //выводит на экран полученное сообщение
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            //выводит в консоль информацию о том, что пользователь подключился к чату
            ConsoleHelper.writeMessage("Участник '" + userName + "' присоединился к чату.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            //выводит в консоль информацию о выходе пользователя из чата
            ConsoleHelper.writeMessage("Участник '" + userName + "' покинул чат.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
            //устанавливает зеачение поля clientConnected внешнего объекта Client в соответствии с
            //переданным параметром
            //оповещает (или "пробуждает ожидающий") основной поток класса Client

            Client.this.clientConnected = clientConnected;
            synchronized (Client.this) {
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            //метод преставляет клиента серверу

            while (true) {
                Message message = connection.receive();
                //получаем сообщение
                if (message.getType() == MessageType.NAME_REQUEST) {
                    String userName = getUserName(); //получает имя пользователя
                    Message newMessage = new Message(MessageType.USER_NAME, userName);
                    //создает новое сообщение и отправляет его серверу
                    connection.send(newMessage);
                } else if (message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    //сообщаем главному потоку, что сервер принял имя, выходим из метода
                    return;
                } else {
                    //если пришло сообщение с каким-либо другим типом, кидает исключение
                    throw new IOException("Unexpected " + message.getType());
                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            //реализует главный цикл обработки сообщений сервера

            while (true) {
                Message message = connection.receive();
                //получаем сообщение от сервера
                if (message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());
                } else if (message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());
                } else if (message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());
                } else {
                    throw new IOException("Unexpected " + message.getType());
                }
            }
        }

        public void run() {
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();
            //получаем адрес и порт сервера

            try {
                Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
                //с помощью полученных данных создаем сокет
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
            }
        }
    }
}
