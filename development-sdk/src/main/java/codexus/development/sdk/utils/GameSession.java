package codexus.development.sdk.utils;
public class GameSession {
    public String NickName,UserId,UserToken;
    public GameSession(String nickName,String userId,String userToken){NickName=nickName;UserId=userId;UserToken=userToken;}
    public String toString(){return (NickName==null?"":NickName)+","+(UserId==null?"":UserId)+","+(UserToken==null?"":UserToken);}
}
