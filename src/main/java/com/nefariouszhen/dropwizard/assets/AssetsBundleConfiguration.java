package com.nefariouszhen.dropwizard.assets;

public interface AssetsBundleConfiguration {
    /**
     * Get the configuration for how the assets should be served.
     *
     * @return The configuration.
     */
    AssetsConfiguration getAssetsConfiguration();
}
