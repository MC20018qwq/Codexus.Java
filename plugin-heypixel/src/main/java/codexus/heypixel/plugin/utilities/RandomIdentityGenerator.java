package codexus.heypixel.plugin.utilities;

import codexus.csharpsup.DotNetRandom;
import java.util.HexFormat;
public final class RandomIdentityGenerator {
    private RandomIdentityGenerator(){}
    public static String generateHex(DotNetRandom random,int count){byte[] b=new byte[count];random.nextBytes(b);return HexFormat.of().withUpperCase().formatHex(b);}
    public static String generateAlphaNumeric(DotNetRandom r,int n){return generate(r,n,"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");}
    public static String generateDigits(DotNetRandom r,int n){return generate(r,n,"0123456789");}
    public static String generateMacAddress(DotNetRandom r,int n){byte[] b=new byte[n];r.nextBytes(b);b[0]&=0xfe;return HexFormat.ofDelimiter(":").formatHex(b);}
    public static String generatePrivateIpv4Address(DotNetRandom r){int first,second;switch(r.next(0,3)){case 0:first=10;second=r.next(0,256);break;case 1:first=172;second=r.next(16,32);break;default:first=192;second=168;}return first+"."+second+"."+r.next(0,256)+"."+r.next(1,255);}
    public static String generateIpv6InterfaceIdentifier(DotNetRandom r){StringBuilder result=new StringBuilder();for(int i=0;i<4;i++){byte[] b=new byte[2];r.nextBytes(b);b[0]&=0xfe;if(i>0)result.append(':');result.append(HexFormat.of().formatHex(b));}return result.toString();}
    private static String generate(DotNetRandom r,int n,String alphabet){char[] result=new char[n];for(int i=0;i<n;i++)result[i]=alphabet.charAt(r.next(alphabet.length()));return new String(result);}
}
