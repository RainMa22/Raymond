package me.rainma22.Raymond.Utils;

import me.rainma22.Raymond.GlobalOptions;

public class RetryUtils {
    public interface RetryingTask {
        public default boolean isSuccess() {
            int nRetries = GlobalOptions.getGlobalOptions().getNumRetries();
            for(int retries = 0; retries < nRetries; retries++){
                if(execOnce()) return true;
            }
            return false;
        }
        boolean execOnce();
    }
    
}
