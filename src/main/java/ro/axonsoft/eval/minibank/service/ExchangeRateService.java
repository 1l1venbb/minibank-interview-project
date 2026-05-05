package ro.axonsoft.eval.minibank.service;

import org.springframework.stereotype.Service;
import ro.axonsoft.eval.minibank.config.ExchangeRateProperties;
import ro.axonsoft.eval.minibank.exception.UnsupportedCurrency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class ExchangeRateService {
    private static final int MONEY_SCALE = 2;
    private static final int EXCHANGE_RATE_SCALE = 6;

    private final ExchangeRateProperties exchangeRateProperties;

    public ExchangeRateService(ExchangeRateProperties exchangeRateProperties) {
        this.exchangeRateProperties = exchangeRateProperties;
    }

    public boolean supportsCurrency(String currency) {
        if (currency == null) {
            return false;
        }

        return exchangeRateProperties.getRates().containsKey(normalizeCurrency(currency));
    }

    public BigDecimal getRate(String currency) {
        String normalizedCurrency = normalizeCurrency(currency);
        BigDecimal rate = exchangeRateProperties.getRates().get(normalizedCurrency);
        if (rate == null) {
            throw new UnsupportedCurrency(normalizedCurrency);
        }

        return rate;
    }

    public String requireSupportedCurrency(String currency) {
        String normalizedCurrency = normalizeCurrency(currency);
        if (!exchangeRateProperties.getRates().containsKey(normalizedCurrency)) {
            throw new UnsupportedCurrency(normalizedCurrency);
        }

        return normalizedCurrency;
    }

    public Map<String, BigDecimal> getRates() {
        return new LinkedHashMap<>(exchangeRateProperties.getRates());
    }

    public ConversionResult convert(BigDecimal amount, String sourceCurrency, String targetCurrency) {
        BigDecimal normalizedAmount = amount.setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);
        String normalizedSourceCurrency = requireSupportedCurrency(sourceCurrency);
        String normalizedTargetCurrency = requireSupportedCurrency(targetCurrency);

        if (normalizedSourceCurrency.equals(normalizedTargetCurrency)) {
            return new ConversionResult(normalizedAmount, null, null);
        }

        BigDecimal sourceToRon = getRate(normalizedSourceCurrency);
        BigDecimal targetToRon = getRate(normalizedTargetCurrency);
        BigDecimal exchangeRate = sourceToRon.divide(targetToRon, EXCHANGE_RATE_SCALE, RoundingMode.HALF_EVEN);
        BigDecimal convertedAmount = normalizedAmount.multiply(exchangeRate).setScale(MONEY_SCALE, RoundingMode.HALF_EVEN);

        return new ConversionResult(convertedAmount, exchangeRate, convertedAmount);
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new UnsupportedCurrency(currency);
        }

        return currency.toUpperCase(Locale.ROOT);
    }

    public record ConversionResult(
            BigDecimal targetAmount,
            BigDecimal exchangeRate,
            BigDecimal convertedAmount
    ) {
    }
}
