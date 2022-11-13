/*
 * This file is/was part of Treasury. To read more information about Treasury such as its licensing, see <https://github.com/ArcanePlugins/Treasury>.
 */

package me.lokka30.treasury.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import me.lokka30.treasury.api.common.event.EventExecutorTrackerShutdown;
import me.lokka30.treasury.api.common.service.Service;
import me.lokka30.treasury.api.common.service.ServiceRegistry;
import me.lokka30.treasury.api.economy.EconomyProvider;
import me.lokka30.treasury.api.economy.misc.OptionalEconomyApiFeature;
import me.lokka30.treasury.plugin.core.TreasuryPlugin;
import me.lokka30.treasury.plugin.core.utils.QuickTimer;
import me.lokka30.treasury.plugin.core.utils.UpdateChecker;
import org.bstats.charts.SimplePie;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

public class TreasuryVelocity {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory;

    @Inject
    public TreasuryVelocity(
            ProxyServer proxy,
            Logger logger,
            @DataDirectory Path dataDirectory,
            Metrics.Factory metricsFactory
    ) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }

    private VelocityTreasuryPlugin treasuryPlugin;

    @Subscribe
    public void onEnable(ProxyInitializeEvent event) {
        final QuickTimer startupTimer = new QuickTimer();

        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        treasuryPlugin = new VelocityTreasuryPlugin(this);
        TreasuryPlugin.setInstance(treasuryPlugin);
        treasuryPlugin.loadMessages();
        treasuryPlugin.loadSettings();
        TreasuryCommand.register(this);

        UpdateChecker.checkForUpdates();
        loadMetrics();

        treasuryPlugin.info("&fStart-up complete (took &b" + startupTimer.getTimer() + "ms&f)");
    }

    private void loadMetrics() {
        Metrics metrics = metricsFactory.make(this, 14651);

        Service<EconomyProvider> service = ServiceRegistry.INSTANCE
                .serviceFor(EconomyProvider.class)
                .orElse(null);

        EconomyProvider economyProvider = service == null ? null : service.get();
        String pluginName = service == null ? null : service.registrarName();

        metrics.addCustomChart(new SimplePie(
                "economy-provider-name",
                () -> economyProvider == null ? "None" : pluginName
        ));

        metrics.addCustomChart(new SimplePie(
                "economy-provider-supports-negative-balances",
                () -> economyProvider == null
                        ? null
                        : Boolean.toString(economyProvider
                                .getSupportedOptionalEconomyApiFeatures()
                                .contains(OptionalEconomyApiFeature.NEGATIVE_BALANCES))
        ));

        metrics.addCustomChart(new SimplePie(
                // unfortunately bStats truncates the length of the id, so the 's' character
                // on the end had to be removed.
                "economy-provider-supports-transaction-events",
                () -> economyProvider == null
                        ? null
                        : Boolean.toString(economyProvider
                                .getSupportedOptionalEconomyApiFeatures()
                                .contains(OptionalEconomyApiFeature.TRANSACTION_EVENTS))
        ));

        metrics.addCustomChart(new SimplePie(
                "plugin-update-checking-enabled",
                () -> Boolean.toString(treasuryPlugin
                        .configAdapter()
                        .getSettings()
                        .checkForUpdates())
        ));

        metrics.addCustomChart(new SimplePie("economy-provider-currencies", () -> {
            if (economyProvider == null) {
                return null;
            }

            final int size = economyProvider.getCurrencies().size();

            if (size >= 10) {
                return "10+";
            } else {
                return Integer.toString(size);
            }
        }));
    }

    @Subscribe
    public void onDisable(ProxyShutdownEvent event) {
        final QuickTimer shutdownTimer = new QuickTimer();

        // make sure to clean up even though we don't have service migration from other service
        // manager to ours as bukkit impl does. that's not really necessary, but it doesn't hurt
        // to be here
        ServiceRegistry.INSTANCE.unregisterAll("Treasury");

        // Shutdown events
        EventExecutorTrackerShutdown.shutdown();

        treasuryPlugin.info("&fShut-down complete (took &b" + shutdownTimer.getTimer() + "ms&f).");
    }

    @Subscribe
    public void onReload(ProxyReloadEvent event) {
        // reloading configurations whenever velocity reloads
        treasuryPlugin.info("A Velocity reload has been detected, reloading Treasury configurations");
        treasuryPlugin.reload();
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

}
