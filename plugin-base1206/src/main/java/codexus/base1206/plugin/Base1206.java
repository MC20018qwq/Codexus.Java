package codexus.base1206.plugin;

import codexus.development.sdk.attributes.Plugin;
import codexus.development.sdk.plugin.IPlugin;
@Plugin(id="FEF71E62-1DA0-4935-AE43-49102690B32F", name="Base1206", description="Support for plugin development in Minecraft 1.20.6", author="DevCodexus", version="1.0.0")
public final class Base1206 implements IPlugin {
    public static final String PluginChannel = "base_1206";
    @Override public void onInitialize() { /* Packet classes are registered by PluginManager. */ }
}
