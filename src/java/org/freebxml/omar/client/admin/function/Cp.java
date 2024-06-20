/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/function/Cp.java,v 1.4 2005/06/07 01:57:10 tonygraham Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin.function;

import org.apache.tools.ant.types.selectors.SelectorUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.freebxml.omar.client.admin.AbstractAdminFunction;
import org.freebxml.omar.client.admin.AdminException;
import org.freebxml.omar.client.admin.AdminFunctionContext;
import org.freebxml.omar.client.admin.AdminShellUtility;
import org.freebxml.omar.client.xml.registry.LifeCycleManagerImpl;
import org.freebxml.omar.client.xml.registry.util.JAXRUtility;
import org.freebxml.omar.common.Utility;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileSystemView;

import javax.xml.registry.BulkResponse;
import javax.xml.registry.infomodel.RegistryObject;
import javax.xml.registry.infomodel.RegistryPackage;


public class Cp extends AbstractAdminFunction {
    private static final Log log = LogFactory.getLog(Cp.class.getName());
    private ArrayList excludes = new ArrayList();
    private ArrayList includes = new ArrayList();
    private String    ownerID;
    protected HashMap saveObjectsSlots = new HashMap();

    public void execute(AdminFunctionContext context, String args)
        throws Exception {
        //The bulk loader MUST turn off versioning because it updates
        //objects in its operations which would incorrectly be created as
        //new objects if versioning is ON when the object is updated.
        saveObjectsSlots.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION, "true");
        saveObjectsSlots.put(bu.CANONICAL_SLOT_LCM_DONT_VERSION_CONTENT, "true");

	this.context = context;

	if (args != null) {
	    String[] tokens = args.split("\\s+");

	    int tIndex = 0;

	    for (tIndex = 0;
		 ((tIndex < tokens.length) && tokens[tIndex].startsWith("-"));
		 tIndex++) {
		String option = tokens[tIndex];

		if (collator.compare(option, "-include") == 0) {
		    if (++tIndex == tokens.length) {
			context.printMessage(getUsage());

			return;
		    }

		    includes.add(tokens[tIndex]);
		} else if (collator.compare(option, "-exclude") == 0) {
		    if (++tIndex == tokens.length) {
			context.printMessage(getUsage());

			return;
		    }

		    excludes.add(tokens[tIndex]);
		} else if (collator.compare(option, "-owner") == 0) {
		    if (++tIndex == tokens.length) {
			context.printMessage(getUsage());

			return;
		    }

		    String owner = tokens[tIndex];

		    if (owner.matches("^%[0-9]+$")) {
			try {
			    int numericOwner =
				Integer.valueOf(owner.substring(1)).intValue();
			    ownerID = context.getUsers()[numericOwner];
			} catch (Exception e) {
			    context.printMessage(format(rb,"invalidIdReference"));
			    return;
			}
		    } else if (Utility.getInstance().isValidURN(owner)) {
			ownerID = owner;
		    } else {
			context.printMessage(format(rb,"invalidIdReference"));
			return;
		    }

		    saveObjectsSlots.put(bu.CANONICAL_SLOT_LCM_OWNER,
					 ownerID);
		} else {
		    context.printMessage(format(rb,"invalidArgument",
						new Object[] { option }));
		    return;
		}
	    }

	    if (tIndex < tokens.length) {
		for ( ; tIndex < tokens.length; tIndex++) {
		    includes.add(tokens[tIndex++]);
		}
	    }
	}

        scanDir(context.getLocalDir(), context.getCurrentRP());
    }

    public String getUsage() {
        return format(rb,"usage.cp");
    }

    /*
     * Check whether fileName is included allowed by the 'include'
     * regex (if any) and not excluded by the 'exclude' regex (if
     * any).
     */
    boolean checkIncludesExcludes(String fileName) {
        // Include unless there's a reason not to.
        boolean canInclude = true;

        if (includes.size() != 0) {
            // If any 'includes' regex, inclusion is no longer automatic.
            canInclude = false;

            for (int pos = 0; pos < includes.size(); pos++) {
		canInclude =
		    SelectorUtils.match((String) includes.get(pos),
					fileName);

                if (context.getDebug()) {
                    context.printMessage(format(rb,"debug.include",
						new Object[] {
							fileName,
							includes.get(pos),
							new Boolean(canInclude)
						}));
                }

		// If matches one include pattern, no need to check more
                if (canInclude) {
                    break;
                }
            }
        }

        if (excludes.size() != 0) {
            for (int pos = 0; pos < excludes.size(); pos++) {
		canInclude =
		    SelectorUtils.match((String) excludes.get(pos),
					fileName);

                if (context.getDebug()) {
                    context.printMessage(format(rb,"debug.exclude",
						new Object[] {
							fileName,
							excludes.get(pos),
							new Boolean(canInclude)
						}));
                }

		// If matches one exclude pattern, no need to check more
                if (!canInclude) {
                    break;
                }
            }
        }

        return canInclude;
    }

    /**
     * Load the contents of baseDir into rp using pathname as base for
     * locators of loaded objects.
     *
     * @param baseDir  Directory in local file system from which to load
     * @param rp       Existing RegistryPackage to which to add
     */
    protected void scanDir(File baseDir, RegistryPackage rootRP)
        throws Exception {
        ArrayList repositoryObjects = new ArrayList();

        LinkedList dirInfoList = new LinkedList();

        dirInfoList.add(new DirInfo(baseDir, rootRP));

        /*
         * Loop through the list of directories (and corresponding
         * RegistryPackages and pathnames).  Child directories of
         * curDir are added to the end of the list, so the list isn't
         * finished until all descendant directories have been
         * processed.
         */
        while (!dirInfoList.isEmpty()) {
            DirInfo curDirInfo = (DirInfo) dirInfoList.removeFirst();
            File curDir = curDirInfo.getDir();
            RegistryPackage curRP = curDirInfo.getRegistryPackage();

            if (!curDir.exists()) {
                throw new AdminException(format(rb, "nonexistentLocalDir",
						new Object[] { curDir }));
            }

            if (!curDir.isDirectory()) {
                throw new AdminException(format(rb, "nondirectoryLocalDir",
						new Object[] { curDir }));
            }

            if (!curDir.canRead()) {
                throw new AdminException(format(rb, "unreadableLocalDir",
						new Object[] { curDir }));
            }

            File[] childFiles = curDir.listFiles();

            for (int i = 0; i < childFiles.length; i++) {
                String childName = childFiles[i].getName();

                boolean canInclude = checkIncludesExcludes(childName);

                RegistryObject childObject;

                if (!canInclude) {
                    if (verbose || debug) {
                        context.printMessage(format(rb,"notIncluding",
						    new Object[] {
							    childFiles[i] }));
                    }

                    continue;
                }

                if (childFiles[i].isFile()) {
                    if (verbose || debug) {
                        context.printMessage(format(rb,"including",
						    new Object[] {
							    "ExtrinsicObject",
							    childFiles[i],
							    childName }));
                    }

                    childObject = context.getService().createExtrinsicObject(childFiles[i]);
                } else if (childFiles[i].isDirectory()) {
                    if (verbose || debug) {
                        context.printMessage(format(rb,"including",
						    new Object[] {
							    "RegistryPackage",
							    childFiles[i],
							    childName }));
                    }

                    childObject = context.getService().createRegistryPackage(childName);

                    dirInfoList.addLast(new DirInfo(childFiles[i],
                            (RegistryPackage) childObject));
                } else {
                    childObject = null;
                    throw new AdminException(format(rb, "notFileOrDir",
						    new Object[] {
							    childFiles[i] }));
                }

                if (curRP != null) {
                    curRP.addRegistryObject(childObject);
                }

                repositoryObjects.add(childObject);
            }
        }

        if (!repositoryObjects.isEmpty()) {
            if (rootRP != null) {
                repositoryObjects.add(rootRP);
            }

            BulkResponse response = ((LifeCycleManagerImpl) context.getService()
                                                                   .getLCM()).saveObjects(repositoryObjects,
                    saveObjectsSlots);

            JAXRUtility.checkBulkResponse(response);
        }
    }

    /**
     * Object representing information about a directory being
     * scanned: the directory, the RegistryPackage associated with the
     * directory, and the pathname to the directory.
     */
    private class DirInfo {
        private File dir;
        private RegistryPackage rp;

        public DirInfo(File dir, RegistryPackage rp) {
            this.dir = dir;
            this.rp = rp;
        }

        public File getDir() {
            return dir;
        }

        public void setDir(File dir) {
            this.dir = dir;
        }

        public RegistryPackage getRegistryPackage() {
            return rp;
        }

        public void setRegistryPackage(RegistryPackage rp) {
            this.rp = rp;
        }
    }
}
