package com.klasha.constants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "com.klasha")
public class ExternalSourceUrl {

    private String populationUrl;
    private String populationFilterUrl;
    private String capitalCityUrl;
    private String locationUrl;
    private String currencyUrl;
    private String isoUrl;
    private String stateUrl;
    private String cityUrl;
}
