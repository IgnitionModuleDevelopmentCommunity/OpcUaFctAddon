package com.bouyguesenergiesservices.gateway.opc;

import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.opc.ServerNodeId;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.gateway.opc.BasicNodeSubscriptionDefinition;
import com.inductiveautomation.ignition.gateway.opc.NodeSubscriptionDefinition;
import com.inductiveautomation.ignition.gateway.opc.SubscribableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by regis on 20/10/2016.
 */
public class SubscribableNodeCallback implements SubscribableNode {

    private final NodeSubscriptionDefinition nodeSubsDef;
    private final String subscriptionName;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AtomicBoolean refresh;
    private QualifiedValue qualifiedValue;
    private Quality quality;


    public SubscribableNodeCallback(ServerNodeId serverNodeId, String subscriptionName, DataType dataType, AtomicBoolean refresh) {
        this.nodeSubsDef = new BasicNodeSubscriptionDefinition(subscriptionName,serverNodeId,dataType);
        this.subscriptionName = subscriptionName;
        this.refresh = refresh;
    }

    @Override
    public NodeSubscriptionDefinition getNodeSubscriptionDefinition() {
        return nodeSubsDef;
    }

    @Override
    public void setValue(QualifiedValue qualifiedValue) {
        this.qualifiedValue = qualifiedValue;
        this.refresh.set(true);
        logger.trace("setValue()> Changement de valeur [subscriptionName:{}, node:{}, value:{}]", subscriptionName, nodeSubsDef.getServerNodeId().toString(), qualifiedValue.getValue().toString());
    }

    @Override
    public void setQuality(Quality quality) {

        if (!quality.equals(this.quality)){
            this.quality = quality;
            this.refresh.set(true);
            logger.trace("setQuality()> Changement d'état [subscriptionName:{}, node:{}, value:{}]", subscriptionName, nodeSubsDef.getServerNodeId().toString(), quality.toString());
        } else{
            logger.trace("setQuality()> Pas de nouveaux changements [subscriptionName:{}, node:{}, value:{}]", subscriptionName, nodeSubsDef.getServerNodeId().toString(), quality.toString());

        }


    }

    @Override
    public QualifiedValue getLastSubscriptionValue() {
        return qualifiedValue;
    }


    public Quality getQuality() {
        return quality;
    }
}
