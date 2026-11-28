package codexus.base12111.plugin;

import codexus.development.sdk.attributes.Plugin;
import codexus.development.sdk.plugin.IPlugin;
@Plugin(id="FEF71E62-1DA0-4935-AE43-49102690B32F", name="Base12111", description="Support for plugin development in Minecraft 1.21.11", author="DevCodexus", version="2.0.0")
public final class Base12111 implements IPlugin {
    public static final String PluginChannel = "base_12111";
    @Override public void onInitialize() { /* Packet classes are registered by PluginManager. */ }
}
