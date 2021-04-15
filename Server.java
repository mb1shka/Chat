package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

    static private Map<String, Connection> connectionMap = new ConcurrentHashMap<>();
    //ключ - имя клиента, значение - соединение с ним

    public static void sendBroadcastMessage(Message message) {
        //отправляет сообщение message всем соединениям из connectionMap
        for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
            try {
                pair.getValue().send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Не удалось отправить сообщение.");
            }
        }
    }



    private static class Handler extends Thread {
        // КЛАСС ДОЛЖЕН РЕАЛИЗОВЫВАТЬ ПРОТОКОЛ ОБЩЕНИЯ С КЛИЕНТОМ

        //тут по идее должен проходить обмен сообщениями с клиентом
        //с помощью создания потока-обработчика Handler после подключения клиента к сокету
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            //знакомство сервера с клиентом
            //в качестве параметра метод принимает соединение connection,
            //а возвращает имя нового клиента

            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));

                Message message = connection.receive();
                if (message.getType() != MessageType.USER_NAME) {
                    ConsoleHelper.writeMessage("Получено сообщение от " + socket.getRemoteSocketAddress() + " ");
                    continue;
                }

                String userName = message.getData();
                //берем имя из сообщения

                if (userName.isEmpty()) {
                    ConsoleHelper.writeMessage("Имя пользователя не должно быть пустым.");
                    continue;
                }

                if (connectionMap.containsKey(userName)) {
                    ConsoleHelper.writeMessage("Такое имя пользователя уже существует.");
                    continue;
                }

                connectionMap.put(userName, connection);
                //если проверки пройдены, добавляем пользователя и его соединения в мапу
                connection.send(new Message(MessageType.NAME_ACCEPTED));
                //отправляем сообщение о том, что имя принято программой и записано

                return userName;
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            //метод отправляет клиенту (новому участнику) информацию об остальных клиентах (участниках) чата

            for (Map.Entry<String, Connection> pair : connectionMap.entrySet()) {
                String newUserName = pair.getKey();
                if (newUserName.equals(userName)) continue;
                connection.send(new Message(MessageType.USER_ADDED, newUserName));
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            //главный цикл обработки сообщений сервером
            //должен принимать сообщение клиента
            while (true) {
                Message message = connection.receive();
                //получаем новое сообщение
                if (message.getType() == MessageType.TEXT) {
                    //если сообщение это текст
                    String data = message.getData();
                    //получаем данные из сообщения
                    sendBroadcastMessage(new Message(MessageType.TEXT, userName + ": " + data));
                    //формируем новое сообщение с именем отправителем и отправляем его в чат
                } else {
                    //если это НЕ текст
                    ConsoleHelper.writeMessage("Что-то пошло не так.");
                }
            }
        }

        public void run() {
            ConsoleHelper.writeMessage("Установлено новое соединение с удаленным адресом: " + socket.getRemoteSocketAddress());
            //выводит сообщение, что новое соединение с удаленным сервером установлено

            String userName = null;
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                //вызываем метод рукопожатия с клиентом, сохраняя информацию об имени нового клиента

                sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));
                //сообщаем участникам о присоединении нового участника

                notifyUsers(connection, userName);
                //сообщаем новому участнику о существующих участниках

                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Ошибка при обмене данных.");
            }

            if (userName != null) {
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, userName));
                //удаляем из connectionMap запись соответствующую userName, отправляем всем участникам
                //чата сообщение о том, что текущий чат был удален
            }
        }
    }




    public static void main(String[] args) {
        //запрашиваем порт сервера, используя ConsoleHelper
        int port = ConsoleHelper.readInt(); //получаем порт, введенный с консоли
        try (ServerSocket serverSocket = new ServerSocket(port)) { //создание серверного сокета с помощью полученного порта
            ConsoleHelper.writeMessage("Сервер запущен.");
            while (true) {
                new Handler(serverSocket.accept()).start();
                //создание и запуск нового потока Handler, передача в конструктор сокета из предыдущего пункта
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Что-то пошло не так.");
        }
    }
}
