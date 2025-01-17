package io.microconfig.core.properties.repository;

import io.microconfig.core.configtypes.ConfigType;
import io.microconfig.core.environments.EnvironmentRepository;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static io.microconfig.utils.CollectionUtils.join;
import static io.microconfig.utils.CollectionUtils.minus;
import static io.microconfig.utils.StreamUtils.flatMapEach;
import static io.microconfig.utils.StringUtils.dotCountIn;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class EnvProfilesComponentGraph implements ComponentGraph {
    private final ComponentGraph delegate;
    private final EnvironmentRepository environmentRepository;

    @Override
    public List<ConfigFile> getConfigFilesOf(String component, String environment, ConfigType configType) {
        List<ConfigFile> standard = delegate.getConfigFilesOf(component, environment, configType);
        List<ConfigFile> profiles = getConfigFilesWithProfilesOf(environment, component, configType);
        return joinConfigs(standard, profiles, environment);
    }

    private List<ConfigFile> getConfigFilesWithProfilesOf(String environment, String component, ConfigType configType) {
        List<String> profiles = environmentRepository.getOrCreateByName(environment).getProfiles();
        return flatMapEach(profiles, p -> getConfigFilesOf(component, p, configType));
    }

    private List<ConfigFile> joinConfigs(List<ConfigFile> standard, List<ConfigFile> profiles, String environment) {
        if (profiles.isEmpty()) return standard;

        List<ConfigFile> filteredProfiles = profiles.stream()
                .filter(doesNotContain(environment))
                .map(c -> isCommonDefaultConfig(c) ? c.withEnvironment(environment) : c)
                .distinct()
                .collect(toList());
        return join(filteredProfiles, minus(standard, filteredProfiles));
    }

    private Predicate<ConfigFile> doesNotContain(String environment) {
        String envSubstring = "." + environment + ".";
        return p -> !p.getFile().getName().contains(envSubstring);
    }

    private boolean isCommonDefaultConfig(ConfigFile c) {
        return dotCountIn(c.getFile().getName()) == 1;
    }

    @Override
    public Optional<File> getFolderOf(String component) {
        return delegate.getFolderOf(component);
    }
}
