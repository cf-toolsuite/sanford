package org.cftoolsuite.cloud.bindings.boot;

import org.springframework.cloud.bindings.Binding;
import org.springframework.cloud.bindings.Bindings;
import org.springframework.cloud.bindings.boot.BindingsPropertiesProcessor;
import org.springframework.core.env.Environment;

import java.util.Map;

public class MinioBindingsPropertiesProcessor implements BindingsPropertiesProcessor {

    /**
     * The {@link Binding} type that this processor is interested in: {@value}.
     **/
    public static final String TYPE = "minio";

    @Override
    public void process(Environment environment, Bindings bindings, Map<String, Object> properties) {
        if (!isTypeEnabled(environment, TYPE)) {
            return;
        }

        bindings.filterBindings(TYPE).forEach(binding -> {
            MapMapper map = new MapMapper(binding.getSecret(), properties);

            map.from("host").to("minio.endpoint.host");
            map.from("port").to("minio.endpoint.port");
            map.from("scheme").to("minio.endpoint.scheme");
            map.from("access-key").to("minio.accessKey");
            map.from("secret-key").to("minio.secretKey");
            map.from("bucket-name").to("minio.bucket.name");
        });

    }

    // @see https://github.com/spring-cloud/spring-cloud-bindings/blob/main/spring-cloud-bindings/src/main/java/org/springframework/cloud/bindings/boot/Guards.java
    private static boolean isTypeEnabled(Environment environment, String type) {
        return environment.getProperty(
                String.format("org.cftoolsuite.cloud.bindings.boot.%s.enable", type),
                Boolean.class, true);
    }

}
