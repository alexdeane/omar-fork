/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2004 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/admin/AdminTool.java,v 1.12 2005/05/17 22:47:42 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.admin;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;
import org.freebxml.omar.client.admin.AdminResourceBundle;

import org.freebxml.omar.common.BindingUtility;
import org.freebxml.omar.common.CommonProperties;
import org.freebxml.omar.common.UUIDFactory;

/**
 * Command-line tool for running an instance of the AdminTool.
 *
 * <p>Command-line parameters are:
 *
 * <dl>
 *
 * <dt><tt>-alias &lt;alias></tt></dt>
 *
 * <dd>Alias to use when accessing user's certificate in keystore.
 * Default alias defined in <tt>jaxr-ebxml.properties</tt> is used if
 * this is not specified.</dd>
 *
 * <dt><tt>-class &lt;adminShellClass></tt></dt>
 *
 * <dd>Class to use for instance of AdminShell.  The default class
 * generated by {@link AdminShellFactory} is used if omitted.</dd>
 *
 * <dt><tt>-command &lt;commands></tt></dt>
 *
 * <dd>Admin tool command sequence to run instead of getting commands
 * from <tt>System.in</tt>.  Separate commands should be separated by
 * a semicolon (;).  It is not necessary to include a 'quit' command
 * in <tt>&lt;commands></tt>.  If you need a ';' that is not a command
 * separator, include it as '\;'.  (Note that the shell in which you
 * run the Admin tool may require the '\' in '\;' to be quoted by a
 * second '\').  If any command contains spaces, the entire command
 * sequence must be enclosed in single or double quotes so the
 * sequence is treated as one command-line parameter instead of
 * several.  If your shell also interprets ';' as separating shell
 * commands, you will always have to quote sequences of multiple admin
 * shell commands.</dd>
 *
 * <dt><tt>-create</tt></dt>
 *
 * <dd>If necessary, create the RegistryPackage specified by the
 * <tt>-root</tt> parameter as well as any parent RegistyPackage
 * objects as needed.</dd>
 *
 * <dt><tt>-debug</tt></dt>
 *
 * <dd>Output extra information that is useful when debugging.</dd>
 *
 * <dt><tt>-keypass &lt;keypass></tt></dt>
 *
 * <dd>Keypass to use when accessing user's certificate in keystore.
 * Default keypass defined in <tt>jaxr-ebxml.properties</tt> is used
 * if this is not specified.</dd>
 *
 * <dt><tt>-localdir &lt;localdir></tt></dt>
 *
 * <dd>Directory in local file system to use as the "working
 * directory" for commands that relate to files in the local file
 * system.</dd>
 *
 * <dt><tt>-locale &lt;locale></tt></dt>
 *
 * <dd>Locale (e.g., "EN" or "FR_ca") to use for selecting the
 * resource bundle to use for error and status messages.  The default
 * is determined by the Java VM.</dd>
 *
 * <dt><tt>-property &lt;name> &lt;value></tt></dt>
 *
 * <dd>Name and value of an additional property that may be used by a
 * particular implementation of {@link AdminShell} or by a particular
 * implementation of {@link AdminShellFunction}.  Implementations will
 * ignore any additional properties that they do not support.</dd>
 *
 * <dt><tt>-registry &lt;url></tt></dt>
 *
 * <dd>URL of ebXML registry to which to connect.  The value of the
 * <tt>jaxr-ebxml.soap.url</tt> property, if specified in
 * <tt>jaxr-ebxml.properties</tt>, is used if omitted, otherwise a
 * built-in default is used.</dd>
 *
 * <dt><tt>-root &lt;locator></tt></dt>
 *
 * <dd>Locator (e.g., "<tt>/registry/userData</tt>") of the
 * RegistryPackage to use as the base for those commands that treat
 * the repository as a tree of RegistryPackage objects that each
 * contain other RegistryObject and RegistryPackage objects.  The
 * default is the RegistryPackage that is defined for user's data:
 * <tt>/registry/userData</tt>.</dd>
 *
 * <dt><tt>-sqlselect &lt;SQL statement></tt></dt>
 *
 * <dd>Execute &lt;SQL statement> to select registry objects.  This
 * should be a complete SQL statement, i.e., it should start with
 * 'select'.  The SQL statement does not have to be terminated by a
 * semicolon (;).</dd>
 *
 * <dt><tt>-v</tt> or <tt>-verbose</tt></dt>
 *
 * <dd>Specifies verbose output of status messages.</dd>
 *
 * </dl>
 */
public class AdminTool {
    private static final Collator collator;
    private static UUIDFactory uuidFactory = UUIDFactory.getInstance();
    static {
        collator = Collator.getInstance();
        collator.setStrength(Collator.PRIMARY);
    }

    static final BindingUtility bu = BindingUtility.getInstance();

    private AdminResourceBundle rb = AdminResourceBundle.getInstance();

    private String    alias;
    private String    adminShellClass;
    private boolean   create;
    private String    command;
    private boolean   debug;
    private ArrayList excludes = new ArrayList();
    private ArrayList includes = new ArrayList();
    private String    keyPass;
    private File      localDir = new File(System.getProperty("user.dir"));
    private String    locale;
    private ArrayList mimetypeMaps = new ArrayList();
    private String    owner;
    private Properties properties;
    private String    registry;
    private String    root = bu.CANONICAL_USERDATA_FOLDER_LOCATOR;
    private String    sqlSelect;
    private boolean   verbose;

    public AdminTool() {
    } // AdminTool constructor

    public static void main(String[] args) {
        try {
            AdminTool adminTool = new AdminTool();
            adminTool.run(args, System.in, System.out);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void run(String[] as, InputStream inStream,  PrintStream outStream)
        throws Exception {
        parseArgs(as);
        doCommands(inStream, outStream);
    }

    void parseArgs(String[] as) {
        //if (as.length == 0) {
	//usage();
	//}

        int i = 0;

        for (i = 0; (i < as.length) && as[i].startsWith("-"); i++) {
            String s = as[i];

	    // '-help' overrides all other parameters.
	    if (collator.compare(s, "-help") == 0) {
		usage();

		return;
	    }

	    if (collator.compare(s, "-alias") == 0) {
		if (++i == as.length) {
		    usage();
		}

		alias = as[i];
	    } else if (collator.compare(s, "-class") == 0) {
		if (++i == as.length) {
		    usage();
		}

		adminShellClass = as[i];
            } else if (collator.compare(s, "-command") == 0) {
		if (++i == as.length) {
		    usage();
		}

		command = as[i];
	    } else if (collator.compare(s, "-create") == 0) {
		create = true;
	    } else if (collator.compare(s, "-debug") == 0) {
		debug = true;
	    } else if (collator.compare(s, "-exclude") == 0) {
		if (++i == as.length) {
		    usage();
		}

		excludes.add(as[i]);
	    } else if (collator.compare(s, "-include") == 0) {
		if (++i == as.length) {
		    usage();
		}

		includes.add(as[i]);
	    } else if (collator.compare(s, "-keypass") == 0) {
		if (++i == as.length) {
		    usage();
		}

		keyPass = as[i];
	    } else if (collator.compare(s, "-localdir") == 0) {
		if (++i == as.length) {
		    usage();
		}

		localDir = new File(as[i]);
	    } else if (collator.compare(s, "-locale") == 0) {
		if (++i == as.length) {
		    usage();
		}

		Locale newLocale = AdminResourceBundle.parseLocale(as[i]);
		Locale.setDefault(newLocale);

		// Make sure common code also uses correct locale.
		CommonProperties.getInstance().put("omar.common.locale",
						   newLocale.toString());
		rb = AdminResourceBundle.getInstance();
	    } else if (collator.compare(s, "-mimetypemap") == 0) {
		if (++i == as.length) {
		    usage();
		}

		mimetypeMaps.add(as[i]);
	    } else if (collator.compare(s, "-owner") == 0) {
		if (++i == as.length) {
		    usage();
		}

		owner = as[i];
	    } else if (collator.compare(s, "-property") == 0) {
		if (++i == as.length) {
		    usage();
		}

		String propertyName = as[i];

		if (++i == as.length) {
		    usage();
		}

		if (properties == null) {
		    properties = new Properties();
		}

		properties.setProperty(propertyName, as[i]);
	    } else if (collator.compare(s, "-registry") == 0) {
		if (++i == as.length) {
		    usage();
		}

		registry = as[i];
	    } else if (collator.compare(s, "-root") == 0) {
		if (++i == as.length) {
		    usage();
		}

		root = as[i];
	    } else if (collator.compare(s, "-sqlselect") == 0) {
		if (++i == as.length) {
		    usage();
		}

		sqlSelect = as[i];
            } else if ((collator.compare(s, "-v") == 0) ||
		       (collator.compare(s, "-verbose") == 0)) {
		verbose = true;
	    } else {
		Object[] formatArgs = { s };
		System.err.println(
			rb.getString(
				AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
				"illegalOption",
				formatArgs));
		usage();
	    }
        }

        if (i < as.length) {
            usage();
        }

	if (debug) {
	    System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					    "debug.args"));
	    for (int arg = 0; arg < as.length; arg++) {
		System.err.println("'" + as[arg] + "'");
	    }
	}
    }

    void doCommands(InputStream inStream, PrintStream outStream) throws AdminException {

	try {
	    if (adminShellClass != null) {
		System.setProperty(AdminShellFactory.ADMIN_SHELL_CLASS_PROPERTY,
				   adminShellClass);
	    }

	    AdminShell adminShell = AdminShellFactory.getInstance().getAdminShell();

	    if (adminShell == null) {
		throw new AdminException(rb.getString(
			  AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
			  "classNotFound",
			  new Object[] {"AdminShell",
					AdminShellFactory.ADMIN_SHELL_CLASS_PROPERTY, 
					ProviderProperties.getInstance().
					getProperty(AdminShellFactory.ADMIN_SHELL_CLASS_PROPERTY)}));
	    }


	    JAXRService service = new JAXRService();

	    service.setDebug(debug);
	    service.setVerbose(verbose);

	    if (alias != null) {
		service.setAlias(alias);
	    }

	    if (keyPass != null) {
		service.setKeyPass(keyPass);
	    }

	    if (registry != null) {
		service.setRegistry(registry);
	    }

	    service.connect();

	    adminShell.setService(service);

	    adminShell.setDebug(debug);
	    adminShell.setRoot(service.getRegistryPackage(root, create));
	    adminShell.setVerbose(verbose);
	    adminShell.setLocalDir(localDir);

	    if (sqlSelect != null) {
		outStream.print(rb.getString(
                        AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                        "doingSQLSelect"));
		adminShell.setSQLSelect(sqlSelect);
		outStream.println(rb.getString(
                        AdminShell.ADMIN_SHELL_RESOURCES_PREFIX + "done"));
	    }

	    if (command != null) {
		outStream.println(rb.getString(
                        AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
                        "executingCommands"));
		adminShell.run(command, outStream);
		outStream.println(rb.getString(
                        AdminShell.ADMIN_SHELL_RESOURCES_PREFIX + "done"));
	    } else {
		adminShell.run(inStream, outStream);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void usage() {
        System.err.println(rb.getString(
                AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +"usage"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.alias"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.class"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.commands"));
        System.err.println("[-debug]");
        System.err.println("[-help]");
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.keypass"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.localdir"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.locale"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.property"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.registry"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.root"));
        System.err.println(rb.getString(AdminShell.ADMIN_SHELL_RESOURCES_PREFIX +
					"usage.sqlselect"));
        System.err.println("[-v | -verbose]");

        System.exit(1);
    }

} // AdminTool