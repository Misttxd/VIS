package cz.rest.dto;

import cz.rest.model.Delivery;
import cz.rest.model.StockLevel;
import java.util.List;

public record StockOverview(
        List<StockLevel> available,
        List<StockLevel> low,
        List<Delivery> deliveries
) {}
