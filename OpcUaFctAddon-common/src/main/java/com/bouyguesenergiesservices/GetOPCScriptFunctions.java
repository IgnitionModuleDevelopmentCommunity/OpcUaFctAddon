package com.bouyguesenergiesservices;


import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.script.hints.ScriptArg;
import com.inductiveautomation.ignition.common.BundleUtil;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;



import java.util.List;

/**
 * Created by regis on 18/10/2016.
 */
public abstract class GetOPCScriptFunctions implements GetOPCRPC {

    static {
        BundleUtil.get().addBundle(
                GetOPCScriptFunctions.class.getSimpleName(),
                GetOPCScriptFunctions.class.getClassLoader(),
                GetOPCScriptFunctions.class.getName().replace('.', '/'));
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public List<QualifiedValue> readValues( @ScriptArg("opcServer") String opcServer,
                         @ScriptArg("lstItemPath") List<String> lstItemPath) {
        return readValuesImpl(opcServer, lstItemPath);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public boolean isSubscribe(@ScriptArg("subscriptionName")String subscriptionName){
        return isSubscribeImpl(subscriptionName);
    }


    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public String subscribe(@ScriptArg("opcServer")String opcServer,
                          @ScriptArg("lstItemPath")List<String> lstItemPath,
                          @ScriptArg("rate")int rate){
        return subscribeImpl(opcServer, lstItemPath, rate);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public List<QualifiedValue> readSubscribeValues(@ScriptArg("subscriptionName")String subscriptionName){
        return readSubscribeValuesImpl(subscriptionName);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public boolean unsubscribe(@ScriptArg("subscriptionName")String subscriptionName){
        return unsubscribeImpl(subscriptionName);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public void unsubscribeAll(){
        unsubscribeAllImpl();
    }


    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public List<QualifiedValue> getRemoteReadValues( @ScriptArg("remoteServer") String remoteServer,
                                                     @ScriptArg("opcServer") String opcServer,
                                                     @ScriptArg("lstItemPath") List<String> lstItemPath) {
        return getRemoteReadValuesImpl(remoteServer,opcServer, lstItemPath);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public boolean getRemoteIsSubscribe( @ScriptArg("remoteServer") String remoteServer,
                                         @ScriptArg("subscriptionName")String subscriptionName){
        return getRemoteIsSubscribeImpl(remoteServer,subscriptionName);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public String getRemoteSubscribe( @ScriptArg("remoteServer") String remoteServer,
                                      @ScriptArg("opcServer")String opcServer,
                                      @ScriptArg("lstItemPath")List<String> lstItemPath,
                                      @ScriptArg("rate")int rate){
        return getRemoteSubscribeImpl(remoteServer,opcServer, lstItemPath, rate);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public List<QualifiedValue> getRemoteReadSubscribeValues( @ScriptArg("remoteServer") String remoteServer,
                                                              @ScriptArg("subscriptionName")String subscriptionName){
        return getRemoteReadSubscribeValuesImpl(remoteServer,subscriptionName);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public boolean getRemoteUnsubscribe( @ScriptArg("remoteServer") String remoteServer,
                                         @ScriptArg("subscriptionName")String subscriptionName){
        return getRemoteUnsubscribeImpl(remoteServer,subscriptionName);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public void getRemoteUnsubscribeAll( @ScriptArg("remoteServer") String remoteServer){
        getRemoteUnsubscribeAllImpl(remoteServer);
    }





    protected abstract List<QualifiedValue> readValuesImpl(String opcServer, List<String> lstItemPath);

    protected abstract boolean isSubscribeImpl(String subscriptionName);

    protected abstract String subscribeImpl(String opcServer, List<String> lstItemPath, int rate);

    protected abstract List<QualifiedValue> readSubscribeValuesImpl(String subscriptionName);

    protected abstract boolean unsubscribeImpl(String subscriptionName);

    protected abstract void unsubscribeAllImpl();

    //GAN Functions Implement
    protected abstract List<QualifiedValue> getRemoteReadValuesImpl(String remoteServer, String opcServer, List<String> lstItemPath);

    protected abstract boolean getRemoteIsSubscribeImpl(String remoteServer, String subscriptionName);

    protected abstract String getRemoteSubscribeImpl(String remoteServer,String opcServer, List<String> lstItemPath, int rate);

    protected abstract List<QualifiedValue> getRemoteReadSubscribeValuesImpl(String remoteServer,String subscriptionName);

    protected abstract boolean getRemoteUnsubscribeImpl(String remoteServer,String subscriptionName);

    protected abstract void getRemoteUnsubscribeAllImpl(String remoteServer);
}
