// ICustomService.aidl
package android.os;

// Declare any non-default types here with import statements

interface ICustomService {
    String sayHello();
    void startBTConnection();
    void stopBTConnection();
    //void sendByBT();
    void sendByBT(inout byte[] content);
    void sendBroadcastByBT(inout byte[] content);
    //void setContext(inout Context ctxt);
}
