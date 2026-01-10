INSERT INTO products (name, description, price, sku_code)
SELECT v.name, v.description, v.price, v.sku_code
FROM (
         VALUES
             ('iPhone 15',  'Apple smartphone',   999.99, 'iphone_15'),
             ('Pixel 8',    'Google smartphone',  799.99, 'pixel_8'),
             ('Galaxy S24', 'Samsung smartphone', 899.99, 'galaxy_24'),
             ('OnePlus 12', 'OnePlus smartphone', 749.99, 'oneplus_12')
     ) AS v(name, description, price, sku_code)
WHERE NOT EXISTS (
    SELECT 1 FROM products p WHERE p.sku_code = v.sku_code
);