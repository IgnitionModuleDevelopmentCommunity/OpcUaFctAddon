package com.bouyguesenergiesservices.opcuafctaddon.designer;

import com.bouyguesenergiesservices.opcuafctaddon.client.GetOPCClientFunctions;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider;

import com.inductiveautomation.ignition.designer.model.DesignerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The main designer entry point to the remote OPC module.
 */
public class DesignerHook extends AbstractDesignerModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Initialize a newly-instantiated script manager.
     * This will be called exactly once.
     * Add a ScriptModule instance "system.byes.opc" to manage by the Designer Instance.
     *
     * @param manager An available manager create by Designer
     */
    @Override
    public void initializeScriptManager(ScriptManager manager){
        super.initializeScriptManager(manager);
        manager.addScriptModule( "system.byes.opc",
                new GetOPCClientFunctions(),
                new PropertiesFileDocProvider());

        logger.trace("initializeScriptManager()");
    }

}
