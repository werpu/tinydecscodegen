package configuration;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "TinyDecsConfig", storages = @Storage("TinyDecsConfig.xml"))
public class ConfigSerializer implements PersistentStateComponent<TinyDecsConfiguration> {

    TinyDecsConfiguration state;

    @Nullable
    @Override
    public TinyDecsConfiguration getState() {
        if(state == null) {
            state = new TinyDecsConfiguration();
        }
        return state;
    }

    @Override
    public void loadState(TinyDecsConfiguration tinyDecsConfiguration) {
        this.state = tinyDecsConfiguration;
    }

    @NotNull
    public static ConfigSerializer getInstance() {
        return ServiceManager.getService(ConfigSerializer.class);
    }

}
