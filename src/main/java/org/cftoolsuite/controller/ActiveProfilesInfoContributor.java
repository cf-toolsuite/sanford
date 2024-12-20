package org.cftoolsuite.controller;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ActiveProfilesInfoContributor implements InfoContributor {

    private final Environment environment;

    public ActiveProfilesInfoContributor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void contribute(Info.Builder builder) {
        String activeProfiles = String.join(",", environment.getActiveProfiles());
        builder.withDetail("active-profiles", activeProfiles);
    }

}
