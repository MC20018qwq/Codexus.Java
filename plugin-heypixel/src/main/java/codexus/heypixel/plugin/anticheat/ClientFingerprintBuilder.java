package codexus.heypixel.plugin.anticheat;

import codexus.csharpsup.*;
import codexus.development.sdk.utils.GameSession;
import codexus.heypixel.plugin.crypto.*;
import codexus.heypixel.plugin.models.*;
import codexus.heypixel.plugin.utilities.*;
import io.netty.buffer.ByteBuf;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static codexus.heypixel.plugin.networking.MessagePackBufferExtensions.*;
public final class ClientFingerprintBuilder {
    private static final String REPORTED_GAME="E:\\MCLDownload\\Game\\.minecraft",REPORTED_JAVA="E:\\MCLDownload\\ext\\jre-v64-220420\\jdk17",REPORTED_MODS=REPORTED_GAME+"\\mods";
    private final String[] processor,product,accounts;
    private final String[][] disks,adapters;
    private final EncryptedFileFingerprint[] encryptedMods;
    private final SaltedDesCipher cipher;
    private final GameSession gameSession;
    private final List<KnownModDescriptor> knownMods=KnownModCatalog.getKnownMods();
    private final DotNetRandom random;
    private final String hardwarePlaceholder;
    public final String EncryptedUserToken;
    public ClientFingerprintBuilder(GameSession session,SaltedDesCipher cipher,String gameId){this(session,cipher,gameId,LauncherGamePaths.CacheRoot);}
    public ClientFingerprintBuilder(GameSession session,SaltedDesCipher cipher,String gameId,Path cacheRoot){
        this.gameSession=session;this.cipher=cipher;random=new DotNetRandom((int)Long.parseLong(session.UserId));
        hardwarePlaceholder=switch(random.next(4)){case 0->"unknown";case 1->"Standard";case 2->"Not Applicable";default->"Default string";};
        processor=SyntheticFingerprintData.ProcessorProfiles.get(random.next(SyntheticFingerprintData.ProcessorProfiles.size())).split("\\|");
        product=generateSystemProductInfo();adapters=generateNetworkAdapterInfo();disks=generateDiskDriveInfo();accounts=generateAccountDirectories();encryptedMods=buildEncryptedModFileFingerprints(cacheRoot,gameId);EncryptedUserToken=UserTokenCipher.encrypt(session.UserToken);
    }
    public void writeInfoReport(ByteBuf b){writeKnownModFingerprints(b);writeString(b,REPORTED_GAME);writeString(b,REPORTED_JAVA);writeProcessorInfo(b);writeSystemProductInfo(b);writeNetworkAdapterInfo(b);writeDiskDriveInfo(b);writeAccountDirectories(b);writeUserIdentity(b);writeEncryptedModFileFingerprints(b);}
    public void writeProcessorInfo(ByteBuf b){writeStringArray(b,processor);}
    public void writeSystemProductInfo(ByteBuf b){writeStringArray(b,product);}
    public void writeNetworkAdapterInfo(ByteBuf b){writeNestedStringArray(b,adapters);}
    public void writeDiskDriveInfo(ByteBuf b){writeNestedStringArray(b,disks);}
    public void writeAccountDirectories(ByteBuf b){writeStringArray(b,accounts);}
    public void writeUserIdentity(ByteBuf b){writeMapHeader(b,1);writeString(b,"UserId",false);long id=Long.parseLong(gameSession.UserId);if((id&0x80000000L)==0)id|=0x80000000L;writeInt64(b,id);}
    public void writeKnownModFingerprints(ByteBuf b){writeArrayHeader(b,knownMods.size());for(var mod:knownMods){writeString(b,mod.ModName,false);writeString(b,mod.ModPath,false);writeString(b,mod.ModHash,false);}}
    public void writeEncryptedModFileFingerprints(ByteBuf b){writeArrayHeader(b,encryptedMods.length);for(var mod:encryptedMods){writeString(b,mod.EncryptedPath(),false);writeString(b,mod.EncryptedSha1(),false);}}
    private EncryptedFileFingerprint[] buildEncryptedModFileFingerprints(Path root,String gameId){
        root=root.toAbsolutePath().normalize();Path shared=root.resolve("GameMods").resolve(gameId).normalize(),instance=root.resolve("Game").resolve(gameId).resolve(".minecraft/mods").normalize();if(!shared.startsWith(root.resolve("GameMods"))||!instance.startsWith(root.resolve("Game")))throw new IllegalArgumentException("Invalid game identifier");
        List<Path> files=new ArrayList<>();for(Path dir:List.of(shared,instance)){if(!Files.isDirectory(dir))continue;try(var paths=Files.list(dir)){paths.filter(p->Files.isRegularFile(p)&&p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".jar")).forEach(files::add);}catch(IOException e){throw new UncheckedIOException(e);}}
        files.sort(Comparator.comparing(p->p.getFileName().toString()));return files.stream().map(this::buildEncryptedModFileFingerprint).toArray(EncryptedFileFingerprint[]::new);
    }
    private EncryptedFileFingerprint buildEncryptedModFileFingerprint(Path file){try{String name=file.getFileName().toString(),hash=HashExtensions.sha1Hex(Files.readAllBytes(file));patchKnownMod(file,name,hash);return new EncryptedFileFingerprint(cipher.encryptString(REPORTED_MODS+"\\"+name),cipher.encryptString(hash));}catch(IOException e){throw new UncheckedIOException(e);}}
    private void patchKnownMod(Path file,String name,String hash){String toml=ModArchiveMetadataReader.readModsToml(file.toString());if(toml==null)return;String id=ModArchiveMetadataReader.parseModId(toml);if(id==null)return;for(var mod:knownMods)if(mod.ModName.equals(id)){mod.ModPath=ModArchiveMetadataReader.replaceFileName(mod.ModPath,name);mod.ModHash=hash;return;}}
    private String[] generateAccountDirectories(){List<String> values=new ArrayList<>();for(int i=0;i<random.next(1,3);i++){String account=RandomIdentityGenerator.generateDigits(random,random.next(12,17))+"@163.com";values.add(Base64.getEncoder().encodeToString(DotNetEncoding.utf8("Game\\"+account)));}return values.toArray(String[]::new);}
    private String[][] generateDiskDriveInfo(){String serial=RandomIdentityGenerator.generateHex(random,2)+"_"+RandomIdentityGenerator.generateHex(random,2)+"_"+RandomIdentityGenerator.generateHex(random,2)+"_"+RandomIdentityGenerator.generateHex(random,2)+".";return new String[][]{{serial.toUpperCase(Locale.ROOT),"\\\\.\\PHYSICALDRIVE0",SyntheticFingerprintData.StorageDeviceModels.get(random.next(SyntheticFingerprintData.StorageDeviceModels.size()))}};}
    private String[][] generateNetworkAdapterInfo(){int count=random.next(1,4);List<String> names=new ArrayList<>(SyntheticFingerprintData.NetworkAdapterNames);ListShuffleExtensions.shuffle(names,random);String[][] pairs=new String[count][2];for(int i=0;i<count;i++){pairs[i][0]=names.get(i);pairs[i][1]=RandomIdentityGenerator.generateMacAddress(random,6);}ListShuffleExtensions.shuffle(names,random);String[][] result=new String[count][];for(int i=0;i<count;i++)result[i]=new String[]{"etc"+(i+1),pairs[i][0],pairs[i][1],"["+RandomIdentityGenerator.generatePrivateIpv4Address(random)+"]","[fe80:0:0:0:"+RandomIdentityGenerator.generateIpv6InterfaceIdentifier(random)+"]"};return result;}
    private String[] generateSystemProductInfo(){String manufacturer=SyntheticFingerprintData.BaseboardManufacturers.get(random.next(SyntheticFingerprintData.BaseboardManufacturers.size()));String identifying=random.next(2)==0?hardwarePlaceholder:RandomIdentityGenerator.generateAlphaNumeric(random,random.next(8,10)).toUpperCase(Locale.ROOT);String version=random.next(2)==1?hardwarePlaceholder:"1.0";byte[] bytes=new byte[16];random.nextBytes(bytes);return new String[]{manufacturer,"unknown",identifying,version,DotNetGuid.fromByteArray(bytes).toString().toUpperCase(Locale.ROOT)};}
    private static void writeStringArray(ByteBuf b,String[] values){writeArrayHeader(b,values.length);for(String value:values)writeString(b,value,false);}
    private static void writeNestedStringArray(ByteBuf b,String[][] values){writeArrayHeader(b,values.length);for(String[] row:values)writeStringArray(b,row);}
}
