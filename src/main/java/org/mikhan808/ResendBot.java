package org.mikhan808;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ResendBot extends TelegramLongPollingBot {

    public static final Pair NEXT=new Pair("/other",">>");
    public static final Pair PREVIOUS=new Pair("/other","<<");
    @Override
    public void onUpdateReceived(Update update) {
        Message msg = update.getMessage();
        Long id = null;
        Chat chat = null;
        if (msg != null) {
            id = msg.getChatId();
            chat = msg.getChat();
            if(msg.getText().contentEquals("/start"))
            {
                sendText(id,BotConfig.START_MESSAGE);
            }else {
                sendText(BotConfig.RESEND_ID,String.format(BotConfig.RESEND_MESSAGE,msg.getText()));
                sendText(id,String.format(BotConfig.REPLY_MESSAGE,msg.getText()));
            }
        } else if (update.hasCallbackQuery()) {
            id = update.getCallbackQuery().getMessage().getChatId();
            chat = update.getCallbackQuery().getMessage().getChat();
        }

    }



    public static int count(String str, String target) {
        return (str.length() - str.replace(target, "").length()) / target.length();
    }




    void sendAdmin(String txt) {
        /*for (Long id : ChurchDB.getAdmins())
            sendText(id, txt);*/
    }


    public void sendText(Long chatId, String text) {
        if (text.length() > 4096) {
            String text1 = text.substring(0, 4095);
            String text2 = text.substring(4095);
            sendText(chatId, text1);
            sendText(chatId, text2);
        } else {
            SendMessage s = new SendMessage();
            s.setChatId(chatId.toString());
            s.setText(text);
            try {
                execute(s);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendFile(Long chatId, File file) {
        SendDocument s = new SendDocument();
        s.setChatId(chatId.toString());
        s.setDocument(new InputFile(file));
        try {
            execute(s);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void sendKeyBoard(Long chatId, String text, List<String> buttons) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 2) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(buttons.get(i));
            if (i + 1 < buttons.size())
                keyboardRow.add(buttons.get(i + 1));
            keyboard.add(keyboardRow);
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendKeyBoard(Long chatId, String text, List<Pair> buttons, boolean flag) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        List<KeyboardRow> keyboard = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 2) {
            KeyboardRow keyboardRow = new KeyboardRow();
            keyboardRow.add(buttons.get(i).second);
            if (i + 1 < buttons.size())
                keyboardRow.add(buttons.get(i + 1).second);
            keyboard.add(keyboardRow);
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    List<List<InlineKeyboardButton>> buildMenu(List<Pair> menu, int startIndex, String postfix) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<InlineKeyboardButton>();
        for (int i = 0, k = startIndex; i < 20 && k < menu.size(); i++, k++) {
            Pair pair = menu.get(k);
            InlineKeyboardButton button = new InlineKeyboardButton(pair.second);
            button.setCallbackData(pair.first);
            buttons.add(button);
            if (buttons.size() >= 2) {
                keyboard.add(buttons);
                buttons = new ArrayList<>();
            }

        }
        if (!buttons.isEmpty())
            keyboard.add(buttons);
        buttons = new ArrayList<>();
        if (startIndex > 0) {
            InlineKeyboardButton button = new InlineKeyboardButton(PREVIOUS.second);
            button.setCallbackData(PREVIOUS.first + (startIndex - 20) + "#" + postfix);
            buttons.add(button);
        }
        if (startIndex + 20 < menu.size()) {
            InlineKeyboardButton button = new InlineKeyboardButton(NEXT.second);
            button.setCallbackData(NEXT.first + (startIndex + 20) + "#" + postfix);
            buttons.add(button);
        }
        if (!buttons.isEmpty())
            keyboard.add(buttons);
        return keyboard;
    }


    public void sendInlineKeyboard(String text, List<Pair> menu, CallbackQuery callbackQuery, int startIndex) {
        EditMessageText message = new EditMessageText();
        message.setChatId(callbackQuery.getMessage().getChatId());
        message.setInlineMessageId(callbackQuery.getInlineMessageId());
        message.setMessageId(callbackQuery.getMessage().getMessageId());
        message.setText(text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        String postfix = callbackQuery.getData();
        if (postfix.contains("#"))
            postfix = postfix.split("#")[1];
        inlineKeyboardMarkup.setKeyboard(buildMenu(menu, startIndex, postfix));
        // Add it to the message
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            // Send the message
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendInlineKeyboard(Long chatId, String text, List<Pair> menu) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(buildMenu(menu, 0, ""));
        // Add it to the message
        message.setReplyMarkup(inlineKeyboardMarkup);

        try {
            // Send the message
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_NAME;
    }

    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }
}
