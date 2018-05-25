package cash.ird.webwallet.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.resource.PathResourceResolver;
import reactor.core.publisher.Mono;

import java.util.Arrays;
 
@Configuration
@EnableConfigurationProperties({ResourceProperties.class})
public class StaticResourcesConfiguration implements WebFluxConfigurer {
 
    private static final String[] STATIC_RESOURCES = new String[]{
        "/**/*.css",
        "/**/*.html",
        "/**/*.js",
        "/**/*.json",
        "/**/*.bmp",
        "/**/*.jpeg",
        "/**/*.jpg",
        "/**/*.png",
        "/**/*.ttf",
        "/**/*.eot",
        "/**/*.svg",
        "/**/*.woff",
        "/**/*.woff2"
    };

    private final ResourceProperties resourceProperties;

    @Autowired
    public StaticResourcesConfiguration(ResourceProperties resourceProperties) {
        this.resourceProperties = resourceProperties;
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //Add all static files
        registry.addResourceHandler(STATIC_RESOURCES)
            .addResourceLocations(resourceProperties.getStaticLocations())
            .setCacheControl(resourceProperties.getCache().getCachecontrol().toHttpCacheControl());
 
        //Create mapping to index.html for Angular HTML5 mode.
        String[] indexLocations = getIndexLocations();
        registry.addResourceHandler("/**")
            .addResourceLocations(indexLocations)
            .setCacheControl(resourceProperties.getCache().getCachecontrol().toHttpCacheControl())
            .resourceChain(true)
            .addResolver(new PathResourceResolver() {
                @Override
                protected Mono<Resource> getResource(String resourcePath, Resource location) {
                    return location.exists() && location.isReadable() ? Mono.just(location) : Mono.empty();
                }
            });
    }
 
    private String[] getIndexLocations() {
        return Arrays.stream(resourceProperties.getStaticLocations())
            .map((location) -> location + "index.html")
            .toArray(String[]::new);
    }
}