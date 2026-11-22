package codexus.cipher.game;

import java.util.Map;

/** Selects the MD5 inputs for local X19 authentication; does not change game installation. */
public final class GameAuthenticationMd5 {
    /** false：使用下方硬编码表；true：下载/更新认证文件，并按实际 JAR、DAT 计算 MD5。 */
    public static final boolean DYNAMIC_DOWNLOAD = false;

    private GameAuthenticationMd5() { }

    public record Md5Pair(String bootstrapMd5, String datFileMd5) { }

    // Restored verbatim from Codexus_Server; keys are NetEase versions, not protocol aliases.
    private static final Map<String, Md5Pair> GAME_MD5 = Map.of(
            "1.7.10", new Md5Pair("A895FE657915D58F55919CEACD30209D", "538D33D5F35EF01736EDA30F94C61DF6"),
            "1.8.9", new Md5Pair("A895FE657915D58F55919CEACD30209D", "0CF2074AA7D4B543E35A3D6BB57AF861"),
            "1.12.2", new Md5Pair("A895FE657915D58F55919CEACD30209D", "51581ADD89B8AC5A0D8CCDD0E33EE1DE"),
            "1.16", new Md5Pair("7B101583C3965371B89A3C9115B27526", "B0712F34B0A584D05D9D29FA68759E29"),
            "1.18", new Md5Pair("C3BD2115F23F6FE4B2ADCC7FC4DEFFEA", "56677A2BB31E18246FA241FB02E16D0E"),
            "1.20", new Md5Pair("2A7A476411A1687A56DC6848829C1AE4", "D285CBF97D9BA30D3C445DBF1C342634"),
            "1.21", new Md5Pair("684528BF492A84489F825F5599B3E1C6", "574033E7E4841D8AC4C14D7FA5E05337"),
            "1.21.8", new Md5Pair("F52CA72E6BCD8D06AAE0A60B81A4C890", "C9906B5809A92C73299279E562A78D81"),
            "1.21.10", new Md5Pair("21A3B8D834A241CB4E3C24A263E35FE7", "CD016E8A0D1930A71E4D58D23C06022A"));

    public static Md5Pair hardcoded(String version) {
        Md5Pair hashes = version == null ? null : GAME_MD5.get(version);
        if (hashes == null) {
            throw new IllegalArgumentException("No hardcoded X19 MD5 entry for gameVersion " + version
                    + "; set GameAuthenticationMd5.DYNAMIC_DOWNLOAD to true or add verified hashes for this version");
        }
        return hashes;
    }

    static Md5Pair resolve(String version, GameArtifactResolver resolver, boolean dynamicDownload) {
        if (!dynamicDownload) return hardcoded(version);
        var files = resolver.resolve(version);
        return new Md5Pair(files.bootstrapMd5(), files.datFileMd5());
    }
}
