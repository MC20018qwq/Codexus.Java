package codexus.heypixel.plugin.models;

import java.util.*;
public final class KnownModCatalog {
    private KnownModCatalog(){}
    // SHA-1 of the supplied NetEase 1.21.11 / NeoForge 21.11.45 client archives.
    // These are archive fingerprints, not proof that a server accepted its private challenges.
    private static final List<KnownModDescriptor> templates=List.of(
        new KnownModDescriptor("minecraft","E:\\MCLDownload\\Game\\.minecraft\\versions\\1.21.11\\1.21.11.jar","ba2df812c2d12e0219c489c4cd9a5e1f0760f5bd"),
        new KnownModDescriptor("neoforge","E:\\MCLDownload\\Game\\.minecraft\\libraries\\net\\neoforged\\neoforge\\21.11.45\\neoforge-21.11.45-universal.jar","6d092491d8297ee97d936256a0bbd7ce1685a6af"),
        new KnownModDescriptor("netease_official","E:\\MCLDownload\\Game\\.minecraft\\mods\\4691455658582475773@3@0.jar","40d7d645d49da50517e9d41bb6395ad655e2491f"),
        new KnownModDescriptor("cloth_config","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283409555448@2@8.jar","e9cb40c59491f8e4c5a87866f152432dfb17642e"),
        new KnownModDescriptor("entityculling","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283419838303@2@8.jar","3701f76db4db78f2dcb57656a2f17b71d7e8c923"),
        new KnownModDescriptor("ferritecore","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283431013280@2@8.jar","900f0f673fef88784711ee952cbf1fe8be2480bd"),
        new KnownModDescriptor("geckolib","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283440973905@2@8.jar","8204b7699118444517efbda4bdb3c1a3f705000c"),
        new KnownModDescriptor("heypixel","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283451864950@2@8.jar","0f53c5654c789f36f8ea3f83d10cb4c138b59108"),
        new KnownModDescriptor("citresewn","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283451864950@2@8.jar","0f53c5654c789f36f8ea3f83d10cb4c138b59108"),
        new KnownModDescriptor("citresewn_defaults","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283451864950@2@8.jar","0f53c5654c789f36f8ea3f83d10cb4c138b59108"),
        new KnownModDescriptor("imblocker","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283528168710@2@8.jar","d3785bc697fdbefddb346f2b1c2200ab56745214"),
        new KnownModDescriptor("iris","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283537562588@2@8.jar","c9a291081c4fba35397e1b30d9b77fc2bffc198c"),
        new KnownModDescriptor("noloadingscreen","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283549425614@2@8.jar","9f24b1464dacc5f61323d2f53e8b1c71f9689407"),
        new KnownModDescriptor("sodium_extra","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283559764813@2@8.jar","93f47a4aa1937c249b6a2b03b9e2b7a836cb2f7c"),
        new KnownModDescriptor("sodium","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283569654467@2@8.jar","de0cd8d51567dc64ccce584ab43e59db6116679e"),
        new KnownModDescriptor("spark","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283580387760@2@8.jar","03112df0197900418b5ff9fcd0947dda94b9e07c"),
        new KnownModDescriptor("tacz","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283593813137@2@8.jar","943123661b3cb59182caa8f99cb51befc6c5db8d"),
        new KnownModDescriptor("waveycapes","E:\\MCLDownload\\Game\\.minecraft\\mods\\4692146283608481695@2@8.jar","fffb04e32dfda2a980778c3e78304c9aa4ff27f1")
    );
    /** Each session patches its own descriptors; concurrent users cannot overwrite each other. */
    public static List<KnownModDescriptor> getKnownMods(){return new ArrayList<>(templates.stream().map(KnownModDescriptor::copy).toList());}
}
