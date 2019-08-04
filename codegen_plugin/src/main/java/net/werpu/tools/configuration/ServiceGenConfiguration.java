/*
 *
 *
 * Copyright 2019 Werner Punz
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software
 * is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 */

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
