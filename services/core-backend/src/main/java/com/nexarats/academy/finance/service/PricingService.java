package com.nexarats.academy.finance.service;

import com.nexarats.academy.finance.model.TuitionPrice;
import com.nexarats.academy.finance.model.TuitionTier;
import com.nexarats.academy.finance.repository.TuitionPriceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Service
public class PricingService {

    private final TuitionPriceRepository tuitionPriceRepository;

    public PricingService(TuitionPriceRepository tuitionPriceRepository) {
        this.tuitionPriceRepository = tuitionPriceRepository;
    }

    @Transactional(readOnly = true)
    public TuitionPrice getTuitionPrice(UUID programId, UUID courseId, String currency, TuitionTier tier) {
        String effectiveCurrency = (currency != null && !currency.isBlank()) ? currency.toUpperCase() : "USD";
        TuitionTier effectiveTier = (tier != null) ? tier : TuitionTier.STANDARD;

        Optional<TuitionPrice> priceOpt;
        if (programId != null) {
            priceOpt = tuitionPriceRepository.findByProgramIdAndCurrencyAndTier(programId, effectiveCurrency, effectiveTier);
            if (priceOpt.isEmpty() && !"USD".equals(effectiveCurrency)) {
                // Fallback to USD base price
                priceOpt = tuitionPriceRepository.findByProgramIdAndCurrencyAndTier(programId, "USD", effectiveTier);
            }
        } else if (courseId != null) {
            priceOpt = tuitionPriceRepository.findByCourseIdAndCurrencyAndTier(courseId, effectiveCurrency, effectiveTier);
            if (priceOpt.isEmpty() && !"USD".equals(effectiveCurrency)) {
                priceOpt = tuitionPriceRepository.findByCourseIdAndCurrencyAndTier(courseId, "USD", effectiveTier);
            }
        } else {
            priceOpt = tuitionPriceRepository.findAll().stream()
                    .filter(p -> p.isActive() && effectiveCurrency.equalsIgnoreCase(p.getCurrency()))
                    .findFirst()
                    .or(() -> tuitionPriceRepository.findAll().stream().filter(TuitionPrice::isActive).findFirst());
        }

        return priceOpt.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No authoritative tuition price configured for target item and currency: " + effectiveCurrency));
    }
}
