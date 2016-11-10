package com.bouyguesenergiesservices.gateway.service;

import com.inductiveautomation.ignition.common.logging.LogEvent;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.gateway.gan.security.TrialPeriodProtected;
import com.inductiveautomation.metro.api.ServerId;
import com.inductiveautomation.metro.impl.services.annotations.FileStream;

import java.util.Date;
import java.util.List;

/**
 * Created by mattgross on 9/19/2016.
 */
public interface GetOPCService {


    List<QualifiedValue> getServiceReadValues(String opcServer, List<String> lstItemPath);

    boolean getServiceIsSubscribe(String subscriptionName);

    String getServiceSubscribe(String opcServer, List<String> lstItemPath, int rate);

    List<QualifiedValue> getServiceReadSubscribeValues(String subscriptionName);

    boolean getServiceUnsubscribe(String subscriptionName);

    void getServiceUnsubscribeAll();

}
