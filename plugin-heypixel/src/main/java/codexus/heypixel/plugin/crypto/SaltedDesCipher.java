package codexus.heypixel.plugin.crypto;

import java.security.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import codexus.csharpsup.DotNetEncoding;
public final class SaltedDesCipher {
    private final String password;
    private static final SecureRandom RANDOM=new SecureRandom();
    public SaltedDesCipher(String password){this.password=password;}
    public String decryptString(String value){return DotNetEncoding.utf8(decrypt(Base64.getDecoder().decode(value)));}
    public String encryptString(String value){return Base64.getEncoder().encodeToString(encrypt(DotNetEncoding.utf8(value)));}
    public byte[] encrypt(byte[] plain){byte[] salt=new byte[8];RANDOM.nextBytes(salt);return encrypt(plain,salt);}
    public byte[] encrypt(byte[] plain,byte[] salt){if(password==null||password.isEmpty())throw new IllegalStateException("Error: 0x00");if(salt.length!=8)throw new IllegalArgumentException("Salt must contain eight bytes");byte[] encrypted=transform(Cipher.ENCRYPT_MODE,plain,salt);byte[] result=Arrays.copyOf(salt,8+encrypted.length);System.arraycopy(encrypted,0,result,8,encrypted.length);return result;}
    public byte[] decrypt(byte[] value){if(password==null||password.isEmpty())throw new IllegalStateException("Error: 0x01");if(value.length<=8)throw new IllegalArgumentException("Error: 0x02");return transform(Cipher.DECRYPT_MODE,Arrays.copyOfRange(value,8,value.length),Arrays.copyOf(value,8));}
    private byte[] transform(int mode,byte[] input,byte[] salt){try{MessageDigest md5=MessageDigest.getInstance("MD5");md5.update(DotNetEncoding.utf8(password));byte[] digest=md5.digest(salt);for(int i=1;i<1000;i++)digest=md5.digest(digest);Cipher cipher=Cipher.getInstance("DES/CBC/PKCS5Padding");cipher.init(mode,new SecretKeySpec(Arrays.copyOf(digest,8),"DES"),new IvParameterSpec(Arrays.copyOfRange(digest,8,16)));return cipher.doFinal(input);}catch(GeneralSecurityException e){throw new IllegalArgumentException("DES operation failed",e);}}
}
