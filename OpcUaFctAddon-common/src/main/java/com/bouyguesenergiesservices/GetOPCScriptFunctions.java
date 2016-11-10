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
    public List<QualifiedValue> readValues(@ScriptArg("remoteServer") String remoteServer,
                                           @ScriptArg("opcServer") String opcServer,
                                           @ScriptArg("lstItemPath") List<String> lstItemPath) {
        return readValuesImpl(remoteServer,opcServer, lstItemPath);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public boolean isSubscribe(@ScriptArg("remoteServer") String remoteServer,
                               @ScriptArg("subscriptionName")String subscriptionName){
        return isSubscribeImpl(remoteServer,subscriptionName);
    }


    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public String subscribe(@ScriptArg("remoteServer") String remoteServer,
                            @ScriptArg("opcServer")String opcServer,
                            @ScriptArg("lstItemPath")List<String> lstItemPath,
                            @ScriptArg("rate")int rate){
        return subscribeImpl(remoteServer,opcServer, lstItemPath, rate);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public List<QualifiedValue> readSubscribeValues(@ScriptArg("remoteServer") String remoteServer,
                                                    @ScriptArg("subscriptionName")String subscriptionName){
        return readSubscribeValuesImpl(remoteServer,subscriptionName);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public boolean unsubscribe(@ScriptArg("remoteServer") String remoteServer,
                               @ScriptArg("subscriptionName")String subscriptionName){
        return unsubscribeImpl(remoteServer,subscriptionName);
    }

    @Override
    @ScriptFunction(docBundlePrefix =  "GetOPCScriptFunctions")
    public void unsubscribeAll(@ScriptArg("remoteServer") String remoteServer){
        unsubscribeAllImpl(remoteServer);
    }


    //Fonctions a implementer non exposees
    protected abstract List<QualifiedValue> readValuesImpl(String remoteServer, String opcServer, List<String> lstItemPath);

    protected abstract boolean isSubscribeImpl(String remoteServer, String subscriptionName);

    protected abstract String subscribeImpl(String remoteServer, String opcServer, List<String> lstItemPath, int rate);

    protected abstract List<QualifiedValue> readSubscribeValuesImpl(String remoteServer, String subscriptionName);

    protected abstract boolean unsubscribeImpl(String remoteServer, String subscriptionName);

    protected abstract void unsubscribeAllImpl(String remoteServer);



}
