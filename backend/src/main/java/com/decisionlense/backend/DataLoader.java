package com.decisionlense.backend;

import com.decisionlense.backend.entity.Sale;
import com.decisionlense.backend.repository.SaleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(SaleRepository repository) {
        return args -> {

            if (repository.count() > 0) {
                return;
            }

            repository.save(createSale(
                    "2026-08-01", "P101", "Electronics", "North",
                    120, 500, 350, 60000
            ));

            repository.save(createSale(
                    "2026-08-02", "P101", "Electronics", "North",
                    118, 500, 350, 59000
            ));

            repository.save(createSale(
                    "2026-08-03", "P101", "Electronics", "North",
                    125, 500, 350, 62500
            ));

            repository.save(createSale(
                    "2026-08-04", "P102", "Electronics", "South",
                    110, 550, 380, 60500
            ));

            repository.save(createSale(
                    "2026-08-05", "P102", "Electronics", "South",
                    115, 550, 380, 63250
            ));

            repository.save(createSale(
                    "2026-08-06", "P103", "Clothing", "North",
                    200, 200, 120, 40000
            ));

            repository.save(createSale(
                    "2026-08-07", "P103", "Clothing", "North",
                    210, 200, 120, 42000
            ));

            repository.save(createSale(
                    "2026-08-08", "P104", "Home", "West",
                    150, 300, 180, 45000
            ));

            repository.save(createSale(
                    "2026-08-09", "P104", "Home", "West",
                    145, 300, 180, 43500
            ));

            repository.save(createSale(
                    "2026-08-10", "P101", "Electronics", "North",
                    100, 500, 350, 50000
            ));

            repository.save(createSale(
                    "2026-08-11", "P101", "Electronics", "North",
                    95, 500, 350, 47500
            ));

            repository.save(createSale(
                    "2026-08-12", "P102", "Electronics", "South",
                    90, 550, 380, 49500
            ));

            repository.save(createSale(
                    "2026-08-13", "P102", "Electronics", "South",
                    85, 550, 380, 46750
            ));

            repository.save(createSale(
                    "2026-08-14", "P103", "Clothing", "North",
                    205, 200, 120, 41000
            ));

            repository.save(createSale(
                    "2026-08-15", "P104", "Home", "West",
                    140, 300, 180, 42000
            ));

            System.out.println("Sales data inserted successfully!");
        };
    }

    private Sale createSale(
            String date,
            String productId,
            String category,
            String region,
            int quantity,
            double unitPrice,
            double cost,
            double revenue) {

        Sale sale = new Sale();

        sale.setDate(LocalDate.parse(date));
        sale.setProductId(productId);
        sale.setCategory(category);
        sale.setRegion(region);
        sale.setQuantity(quantity);
        sale.setUnitPrice(unitPrice);
        sale.setCost(cost);
        sale.setRevenue(revenue);

        return sale;
    }
}