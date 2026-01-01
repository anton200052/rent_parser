package me.vasylkov.rentparser.component;

import lombok.extern.slf4j.Slf4j;
import me.vasylkov.rentparser.entity.ImmoScoutListing;
import me.vasylkov.rentparser.entity.Listing;
import me.vasylkov.rentparser.model.TaskInfoSnapshot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramMessagesSender implements Notificator {

    @Override
    public void sendListings(List<Listing> listings, TaskInfoSnapshot taskInfo) {
        if (listings == null || listings.isEmpty()) {
            log.info("No listings to send.");
            return;
        }

        List<String> messages = buildListingMessages(listings);

        for (String message : messages) {
            sendMessage(message, taskInfo);
        }

        log.info("{} new listings sent.", messages.size());
    }

    @Override
    public void sendMessage(String message, TaskInfoSnapshot taskInfo) {
        String botToken = taskInfo.telegramToken();
        TelegramClient client = new OkHttpTelegramClient(botToken);

        List<String> chatIds = taskInfo.chatIds();
        for (String chatId : chatIds) {
            SendMessage send = new SendMessage(chatId, message);
            send.setParseMode("MarkdownV2");          // важный момент
            try {
                client.execute(send);
            } catch (TelegramApiException e) {
                log.error("Error while sending Telegram message!", e);
            }
        }
    }


    private List<String> buildListingMessages(List<Listing> listings) {
        List<String> messages = new ArrayList<>();
        if (listings == null || listings.isEmpty()) return messages;

        for (Listing listing : listings) {
            StringBuilder sb = new StringBuilder();

            sb.append("🏠 *").append(esc("Цена:")).append("* ")
                    .append(esc(String.valueOf(listing.getPrice()))).append("\n");

            if (listing.getRoomsValue() != null)
                sb.append("🛏 ").append(esc("Комнат: "))
                        .append(esc(String.valueOf(listing.getRoomsValue()))).append("\n");

            if (listing.getAreaSqMeters() != null)
                sb.append("📏 ").append(esc("Площадь: "))
                        .append(esc(String.valueOf(listing.getAreaSqMeters())))
                        .append(" ").append(esc("м²")).append("\n\n");

            if (listing.getLocation() != null)
                sb.append("⏱ ").append(esc("Опубликовано: "))
                        .append(esc(listing.getPublished())).append("\n\n");

            if (listing.getLocation() != null)
                sb.append("📍 ").append(esc("Местоположение: "))
                        .append(esc(listing.getLocation())).append("\n");

            if (listing.getDescription() != null && !listing.getDescription().isBlank())
                sb.append("\uD83D\uDCDD ").append(esc("Описание: "))
                        .append(esc(listing.getDescription())).append("\n\n");

            if (listing instanceof ImmoScoutListing immoScoutListing) {
                if (immoScoutListing.getPlusRequired() != null)
                    sb.append("⭐ ").append(esc("ImmoScout+ подписка: "))
                            .append(esc(String.valueOf(immoScoutListing.getPlusRequired()))).append("\n\n");
            }

            if (listing.getLink() != null) {
                sb.append("[")
                        .append(esc("Открыть объявление"))
                        .append("](")
                        .append(escUrl(listing.getLink()))
                        .append(")\n");
            }

            messages.addFirst(sb.toString());
        }
        return messages;
    }

    /** Экранирование для MarkdownV2: MUST для всей видимой строки */
    private String esc(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")   // <-- твоя ошибка была из-за точки
                .replace("!", "\\!");
    }

    /** В URL внутри ( ... ) обычно достаточно экранировать круглые скобки и пробелы */
    private String escUrl(String url) {
        if (url == null) return "";
        // безопасно: экранируем скобки и пробелы, остальное оставляем
        return url.replace("(", "\\(")
                .replace(")", "\\)")
                .replace(" ", "%20");
    }
}
