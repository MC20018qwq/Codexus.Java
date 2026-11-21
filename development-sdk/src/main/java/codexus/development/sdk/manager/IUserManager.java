package codexus.development.sdk.manager;
import codexus.development.sdk.entities.EntityAvailableUser;
public interface IUserManager {
    final class Current {
        private Current() { }
        public static IUserManager Instance;
        public static IUserManager CppInstance;
    }
    EntityAvailableUser getAvailableUser(String entityId);
}
