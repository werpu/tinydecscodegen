package net.werpu.tools.configuration;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import net.werpu.tools.gui.ConfigServiceGen;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ServiceGenConfiguration implements Configurable {

    private ConfigServiceGen configPanel;
    private TinyDecsConfiguration originalConfig;

    @Nls
    @Override
    public String getDisplayName() {
        return "Tiny Decorations Configuration";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        configPanel = new ConfigServiceGen();
        originalConfig = ConfigSerializer.getInstance().getState();
        configPanel.getReturnValueLevel().setValue(originalConfig.getReturnValueStripLevel());
        configPanel.getSourceFramework().setSelectedItem(originalConfig.getSourceRestFramework());
        configPanel.getTargetFramework().setSelectedItem(originalConfig.getTargetClientFramework());
        return configPanel.getRootPanel();
    }

    @Override
    public boolean isModified() {
        return !originalConfig.equals(getCurrentlySetupConfig());
    }

    private TinyDecsConfiguration getCurrentlySetupConfig() {
        return new TinyDecsConfiguration((Integer) configPanel.getReturnValueLevel().getValue(),
                (String) configPanel.getSourceFramework().getSelectedItem(),
                (String) configPanel.getTargetFramework().getSelectedItem(), false, false,
                false, false, false, false,
                false, "GET", true, true, true, "", "");
    }

    @Override
    public void apply() throws ConfigurationException {
        ConfigSerializer.getInstance().loadState(getCurrentlySetupConfig());

    }

    @Override
    public void disposeUIResources() {
        configPanel = null;
    }
}
