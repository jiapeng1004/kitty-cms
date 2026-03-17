package icu.jiapeng.kitty.dns;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Map;
import java.util.Optional;

@EnableConfigurationProperties(LocalDnsProperties.class)
public class LocalDnsAutoConfiguration {

    @Bean
    @Primary
    @Conditional(LocalDnsEnabledCondition.class)
    public HttpServiceProxyFactory httpServiceProxyFactory(LocalDnsProperties properties) {
        Map<String, LocalDnsClientHttpRequestInterceptor.Target> targets = properties.getMappings().entrySet().stream()
                .map(entry -> {
                    String alias = entry.getKey();
                    String hostPort = entry.getValue();
                    Optional<LocalDnsClientHttpRequestInterceptor.Target> targetOpt =
                            LocalDnsClientHttpRequestInterceptor.Target.fromHostPort(hostPort);
                    return targetOpt.map(target -> Map.entry(alias, target));
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        RestClient.Builder restClientBuilder = RestClient.builder();
        if (!targets.isEmpty()) {
            restClientBuilder.requestInterceptor(new LocalDnsClientHttpRequestInterceptor(targets));
        }

        RestClient restClient = restClientBuilder.build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
    }
}

