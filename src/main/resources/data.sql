-- Clientes de prueba
INSERT INTO clientes (nombre, rut, email, telefono) VALUES ('Diego Loyola', '11111111-1', 'diego@email.com', '+56912345678');
INSERT INTO clientes (nombre, rut, email, telefono) VALUES ('Ángel Montecino', '22222222-2', 'angel@email.com', '+56987654321');
INSERT INTO clientes (nombre, rut, email, telefono) VALUES ('Benjamín Castillo', '33333333-3', 'benjamin@email.com', '+56911223344');

-- Productos
INSERT INTO productos (sku, nombre, descripcion, categoria, precio, stock_actual, stock_minimo) 
VALUES ('PER-001', 'Teclado Mecánico 60%', 'Switches Red', 'Periféricos', 45000.00, 50, 10);

INSERT INTO productos (sku, nombre, descripcion, categoria, precio, stock_actual, stock_minimo) 
VALUES ('MON-001', 'Monitor 24" 144Hz', 'Panel IPS', 'Monitores', 120000.00, 2, 5);

INSERT INTO productos (sku, nombre, descripcion, categoria, precio, stock_actual, stock_minimo) 
VALUES ('PER-002', 'Mouse Inalámbrico', 'Ergonómico', 'Periféricos', 25000.00, 30, 10);
