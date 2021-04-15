package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BotClient extends Client {

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
        //переопределенный метод из класса Client
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
        //возвращает false, так как бот не пишет в консоль
    }

    @Override
    protected String getUserName() {
        //генерируем новое имя бота
        int X = (int) (Math.random() * 100);
        return "date_bot_" + X;
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    public class BotSocketThread extends SocketThread {

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            //отправляет сообщение
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
            //представляем бота
        }

        @Override
        protected void processIncomingMessage(String message) {
            //обрабатывает полученные сообщения
            //выводит в консоль текст полученного сообщения
            ConsoleHelper.writeMessage(message);

            //получаем из message имя отправителя и текст сообщения
            String userNameDelimiter = ": ";
            String[] split = message.split(userNameDelimiter);
            if (split.length != 2) return;
            //выходим из метода, если в массиве оказалось не два эдемента, то есть не формат имя \ текст сообщения

            String onlyTextWithoutName = split[1];
            //получаем только само сообщение

            String format = null;
            switch (onlyTextWithoutName) {
                case "дата":
                    format = "d.MM.YYYY";
                    break;
                case "день":
                    format = "d";
                    break;
                case "месяц":
                    format = "MMMM";
                    break;
                case "год":
                    format = "YYYY";
                    break;
                case "время":
                    format = "H:mm:ss";
                    break;
                case "час":
                    format = "H";
                    break;
                case "минуты":
                    format = "m";
                    break;
                case "секунды":
                    format = "s";
                    break;
            }
            //тут подготовили формат

            if (format != null) {
                String answer = new SimpleDateFormat(format).format(Calendar.getInstance().getTime());
                BotClient.this.sendTextMessage("Информация для " + split[0] + ": " + answer);
            }
        }
    }
}
