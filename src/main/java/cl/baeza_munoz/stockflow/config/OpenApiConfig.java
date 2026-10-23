package cl.baeza_munoz.stockflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stockflowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("StockFlow API - GlobalRetail S.A.")
                        .description("API REST para la gestión de catálogo de productos, control de inventario, administración de clientes y compras transaccionales.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo StockFlow")
                                .email("contacto@stockflow.cl")));
    }
}
