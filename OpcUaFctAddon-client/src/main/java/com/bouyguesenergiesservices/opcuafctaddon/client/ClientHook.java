package com.bouyguesenergiesservices.opcuafctaddon.client;


import com.inductiveautomation.vision.api.client.AbstractClientModuleHook;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHook extends AbstractClientModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Initialize a newly-instantiated script manager.
     * This will be called exactly once.
     * Add a ScriptModule instance "system.byes.opc" to manage by the Client Instance.
     *
     * @param manager An available manager create by Client
     */
    @Override
    public void initializeScriptManager(ScriptManager manager) {
        super.initializeScriptManager(manager);
        manager.addScriptModule(
                "system.byes.opc",
                new GetOPCClientFunctions(),
                new PropertiesFileDocProvider());
        logger.trace("initializeScriptManager()");
    }

}
