package cz.rest.service;

import cz.rest.dto.DeliveryRequest;
import org.springframework.transaction.annotation.Transactional;
import cz.rest.model.Delivery;
import cz.rest.model.Ingredient;
import cz.rest.model.StockLevel;
import cz.rest.dao.DeliveryDao;
import cz.rest.dao.PostgresIngredientDao;
import java.util.ArrayList;
import cz.rest.dto.StockOverview;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class StockService {
    private final DeliveryDao deliveryDao;
    private final PostgresIngredientDao postgresIngredientDao;

    public StockService(DeliveryDao deliveryDao,PostgresIngredientDao postgresIngredientDao) {
        this.deliveryDao = deliveryDao;
        this.postgresIngredientDao = postgresIngredientDao;
    }

    @Transactional
    public int receive(DeliveryRequest delivery) {
        if (delivery.remaining() == null || delivery.remaining().signum() <= 0) {
            throw new IllegalArgumentException("Příjem musí být větší než nula.");
        }
        Integer ingredientId = delivery.ingredientId();
        if (ingredientId == null) {
            if (delivery.name() == null || delivery.name().isBlank() || delivery.categoryId() == null || delivery.unit() == null) {
                throw new IllegalArgumentException("Vyplňte název, kategorii a jednotku nové suroviny.");
            }
            Ingredient ingredient = new Ingredient(null, delivery.name(), delivery.categoryId(), delivery.unit(), BigDecimal.ZERO);
            ingredientId = postgresIngredientDao.create(ingredient);
        }
        return deliveryDao.receive(new Delivery(null, ingredientId, delivery.remaining(), delivery.expires()));
    }

    public void withdraw(int deliveryId, BigDecimal quantity) {
        if (quantity.signum() <= 0 || deliveryDao.withdraw(deliveryId, quantity) == 0) {
            throw new IllegalArgumentException("Zadejte kladné množství do výše dostupné zásoby.");
        }
    }

    public List<Delivery> deliveries() {
        return deliveryDao.getAllItems();
    }

    public List<StockLevel> levels(LocalDate day) {
        return deliveryDao.levels(day);
    }

    public StockOverview overview(LocalDate day) {
        List<StockLevel> available = new ArrayList<>();
        List<StockLevel> low = new ArrayList<>();
        for (StockLevel stock : levels(day)) {
            if (stock.available().signum() > 0) {
                available.add(stock);
            }
            if (stock.low()) {
                low.add(stock);
            }
        }
        return new StockOverview(available, low, deliveryDao.remainingDeliveries());
    }

    public List<Delivery> expiring(LocalDate day) {
        List<Delivery> result = new ArrayList<>();
        for (Delivery delivery : deliveries()) {
            if (delivery.remaining().signum() > 0 && !delivery.expires().isBefore(day)
                    && !delivery.expires().isAfter(day.plusDays(3))) {
                result.add(delivery);
            }
        }
        return result;
    }
}
