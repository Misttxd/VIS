package cz.gastro.dto;

import cz.gastro.model.Delivery;
import cz.gastro.model.StockLevel;
import java.util.List;

public record StockOverview(
        List<StockLevel> available,
        List<StockLevel> low,
        List<Delivery> deliveries
) {}
