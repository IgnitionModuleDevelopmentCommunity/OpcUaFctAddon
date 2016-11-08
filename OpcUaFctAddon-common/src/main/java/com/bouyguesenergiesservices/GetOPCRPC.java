package com.bouyguesenergiesservices;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;

import java.util.List;

/**
 * Created by regis on 18/10/2016.
 *
 */
public interface GetOPCRPC {

    List<QualifiedValue> readValues(String opcServer, List<String> lstItemPath);

    boolean isSubscribe(String subscriptionName);

    String subscribe(String opcServer, List<String> lstItemPath, int rate);

    List<QualifiedValue> readSubscribeValues(String subscriptionName);

    boolean unsubscribe(String subscriptionName);

    void unsubscribeAll();

    List<QualifiedValue> getRemoteReadValues(String remoteServer,String opcServer, List<String> lstItemPath);

    boolean getRemoteIsSubscribe(String remoteServer,String subscriptionName);

    String getRemoteSubscribe(String remoteServer,String opcServer, List<String> lstItemPath, int rate);

    List<QualifiedValue> getRemoteReadSubscribeValues(String remoteServer,String subscriptionName);

    boolean getRemoteUnsubscribe(String remoteServer,String subscriptionName);

    void getRemoteUnsubscribeAll(String remoteServer);

}
