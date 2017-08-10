package com.bouyguesenergiesservices.opcuafctaddon.gateway.opc;

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
 *
 * An object that provides the information needed to subscribe to a node (the NodeSubscriptionDefinition), and receives the QualifiedValue when the subscribed node changes.
 */
public class SubscribableNodeCallback implements SubscribableNode {

    private final NodeSubscriptionDefinition nodeSubsDef;
    private final String subscriptionName;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AtomicBoolean refresh;
    private QualifiedValue qualifiedValue = null;
    private Quality quality = null;



    public SubscribableNodeCallback(ServerNodeId serverNodeId, String subscriptionName, DataType dataType, AtomicBoolean refresh) {
        this.nodeSubsDef = new BasicNodeSubscriptionDefinition(subscriptionName,serverNodeId,dataType);
        this.subscriptionName = subscriptionName;
        this.refresh = refresh;
    }

    /**
     * Retrieves the node subscription definition of what this node wants to subscribe to
     *
     * @return NodeSubscriptionDefinition Provides the meta data needed for an OPC server to subscribe to a node
     */
    @Override
    public NodeSubscriptionDefinition getNodeSubscriptionDefinition() {
        return nodeSubsDef;
    }

    /**
     * Sets a new value for this subscribable node.
     *
     * @param qualifiedValue Represents a value with a DataQuality and timestamp attached to it.
     */
    @Override
    public void setValue(QualifiedValue qualifiedValue) {
        if (!qualifiedValue.equals(this.qualifiedValue)) {
            this.qualifiedValue = qualifiedValue;
            this.refresh.set(true);// record a change on the subscription owner
            logger.trace("setValue()> Changement de valeur [subscriptionName:{}, node:{}, value:{}]", subscriptionName, nodeSubsDef.getServerNodeId().toString(), qualifiedValue.getValue().toString());
        }
    }

    /**
     * Sets only the quality.
     *
     * @param quality the quality element
     */
    @Override
    public void setQuality(Quality quality) {

        if (!quality.equals(this.quality)){
            this.quality = quality;
            this.refresh.set(true);// record a change on the subscription owner
            logger.trace("setQuality()> Changement d'état [subscriptionName:{}, node:{}, value:{}]", subscriptionName, nodeSubsDef.getServerNodeId().toString(), quality.toString());
        } else{
            logger.trace("setQuality()> Pas de nouveaux changements [subscriptionName:{}, node:{}, value:{}]", subscriptionName, nodeSubsDef.getServerNodeId().toString(), quality.toString());
        }

    }

    /**
     * Retrieve the last value set by setValue(QualifiedValue) (possibly modified by a call to setQuality(StatusCode).
     *
     * @return QualifiedValue Represents a value with a DataQuality and timestamp attached to it.
     */
    @Override
    public QualifiedValue getLastSubscriptionValue() {
        return qualifiedValue;
    }


    public String toString(){
        return nodeSubsDef.getServerNodeId().getNodeId().toString();
    }


}
