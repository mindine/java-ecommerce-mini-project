package com.ecommerce.service;

import com.ecommerce.Product;
import com.ecommerce.db.dao.ProductDao;

import java.sql.SQLException;
import java.util.List;

public class CatalogService {
    public List<Product> loadCatalog() throws SQLException {
        return new ProductDao().getAllProducts();
    }
}
