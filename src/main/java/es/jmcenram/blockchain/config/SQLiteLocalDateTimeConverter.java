package es.jmcenram.blockchain.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Converter(autoApply = true)
public class SQLiteLocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {

    private static final DateTimeFormatter DB_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final List<DateTimeFormatter> READ_FORMATTERS = List.of(
            DB_FORMATTER,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ISO_LOCAL_DATE_TIME
    );

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        return attribute == null ? null : attribute.format(DB_FORMATTER);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();

        if (value.matches("-?\\d+")) {
            long epochValue = Long.parseLong(value);
            if (Math.abs(epochValue) < 10_000_000_000L) {
                epochValue *= 1_000L;
            }
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochValue), ZoneId.systemDefault());
        }

        for (DateTimeFormatter formatter : READ_FORMATTERS) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported SQLite date format.
            }
        }

        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha SQLite no soportada: " + value, e);
        }
    }
}
