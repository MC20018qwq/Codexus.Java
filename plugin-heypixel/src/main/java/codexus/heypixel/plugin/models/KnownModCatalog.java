package codexus.heypixel.plugin.models;

import java.util.*;
public final class KnownModCatalog {
    private KnownModCatalog(){}
    private static final List<KnownModDescriptor> templates=List.of(
        new KnownModDescriptor("minecraft","E:\\MCLDownload\\Game\\.minecraft\\libraries\\net\\minecraft\\client\\1.20.1-20230612.114412\\client-1.20.1-20230612.114412-srg.jar","3c8aa19b710a3a68f721210eb69b74594d13e218"),
        new KnownModDescriptor("saturn","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660717756909@2@8.jar","4c5826a73bdbc052db2fe080c582e59ae8ed227d"),
        new KnownModDescriptor("viaforge","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660735087753@2@8.jar","9df7487be88cc018ab295618c1f88a0a4886608f"),
        new KnownModDescriptor("geckolib","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660633006665@2@8.jar","040e42eb566a764792ba0c77e39a56f020cabb8c"),
        new KnownModDescriptor("heypixel","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660643769709@2@8.jar","e66b8fba3b1a772885244eaf2dacd2e9b241d79f"),
        new KnownModDescriptor("entityculling","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660614912528@2@8.jar","09c17c8794a0e00d2ccb51b8d7c1b812498c5c33"),
        new KnownModDescriptor("mixinextras","","5c35573a7b76103724799ca1174a69699b8a9aec"),
        new KnownModDescriptor("netease_official","E:\\MCLDownload\\Game\\.minecraft\\mods\\4681704866889354274@3@0.jar","a9c7c9e3ff02a30f1920b65ee85af9c5e7c25a45"),
        new KnownModDescriptor("waveycapes","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660750230557@2@8.jar","c838d45bd4bb53ed66d66ed3cf3822a7ebd7ba09"),
        new KnownModDescriptor("ferritecore","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660623916601@2@8.jar","417fb6ce8f52abf40bd9d0390371790f9576f8ba"),
        new KnownModDescriptor("embeddium_extra","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660708237753@2@8.jar","bb422d5626bf69841e444a3e545c8e71b6928cf8"),
        new KnownModDescriptor("cloth_config","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660593985565@2@8.jar","c65d07748acc57ceb45d53b3964368b84f34d54f"),
        new KnownModDescriptor("forge","E:\\MCLDownload\\Game\\.minecraft\\libraries\\net\\minecraftforge\\forge\\1.20.1-47.3.0\\forge-1.20.1-47.3.0-universal.jar","7aeb6f58286b5398dc7dfa1d3db1757e601145c1"),
        new KnownModDescriptor("embeddium","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660604590690@2@8.jar","b772ec4f03fbe9b218e9f28d9441ea0a5724d5cf"),
        new KnownModDescriptor("oculus","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660697310016@2@8.jar","27410903d3af950378776106b76503cfebe7ea3a"),
        new KnownModDescriptor("imblocker","E:\\MCLDownload\\Game\\.minecraft\\mods\\4685717660687660960@2@8.jar","65c67fd0c62ad2198902f451bed1f02a5fd15aa5")
    );
    /** Each session patches its own descriptors; concurrent users cannot overwrite each other. */
    public static List<KnownModDescriptor> getKnownMods(){return new ArrayList<>(templates.stream().map(KnownModDescriptor::copy).toList());}
}
