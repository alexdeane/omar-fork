/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2003 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/client/ui/swing/HyperLinker.java,v 1.4 2005/10/28 06:36:39 dougb62 Exp $
 * ====================================================================
 */
package org.freebxml.omar.client.ui.swing;

import org.freebxml.omar.client.xml.registry.util.ProviderProperties;

import java.io.IOException;

/*
 * Based upon code from:
 * http://forum.java.sun.com/thread.jsp?thread=328882&forum=57&message=1337973
 *
 * Class is used to open a browser and show a defined URL/FILE
 * if not running in an Applet. (Running
 * in an Applet this will not work, use myApplet.getAppletContext
 * ().showDocument(URL);
 * If a browser window is opened, it will be reused to show the
 * document (no new one will be opened)
 */
public class HyperLinker {
    private static JavaUIResourceBundle rb =
	JavaUIResourceBundle.getInstance();
    private static ProviderProperties props = ProviderProperties.getInstance();

    // Used to identify the windows platform.
    private static final String WIN_ID = "windows";

    // The default system browser under windows.
    private static final String DEF_WIN_COMMAND =
	"rundll32 url.dll,FileProtocolHandler $url";

    // The default browser under unix.
    private static final String UNIX_PATH = "firefox ";
    private static final String DEF_UNIX_COMMAND =
	UNIX_PATH + "-remote openURL($url)";

    /**
     * Display a file in the system browser. If you want to
     * display a
     * file, you must include the absolute path name.
     *
     * @param url the file's url (the url must start with
     * either "http://" or * "file://").
     * @return is true, if displaying was possible (at leas no
     * error occures)
     */
    public static boolean displayURL(String url) {
        boolean result = true;
        boolean windows = isWindowsPlatform();
        String cmd =
	    props.getProperty("jaxr-ebxml.registryBrowser.webBrowser.launch");

        try {
            if (cmd == null) {
                // Set cmd from hardwired platform specific defaults
                if (windows) {
                    cmd = DEF_WIN_COMMAND;
                } else {
                    cmd = DEF_UNIX_COMMAND;
                }
            }

	    //Replace url parameter with actual URL
	    int index = cmd.indexOf("$url");
	    if (index != -1) {
		cmd = cmd.substring(0, index) + url + cmd.substring(index + 4);
	    }

	    Process p = Runtime.getRuntime().exec(cmd);
            if (!windows) {
                // Under Unix, Netscape/Mozilla has to be running for the "-
                //remote"
                // command to work. So, we try sending the command and
                // check for an exit value. If the exit command is 0,
                // it worked, otherwise we need to start the browser.
                // cmd = 'netscape -remote openURL
                //(http://www.javaworld.com)'
                try {
                    // wait for exit code -- if it's 0, command worked,
                    // otherwise we need to start the browser up.
                    int exitCode = p.waitFor();

                    if (exitCode != 0) {
                        // Command failed, start up the browser
                        // cmd2 = 'firefox http://www.javaworld.com'
			// ??? Should probably add property for fallback cmd
                        String cmd2 = UNIX_PATH + url;
			RegistryBrowser.displayInfo(rb.
			        getString("message.info.failedToLaunchBrowser",
					  new Object []{cmd, cmd2}));

                        p = Runtime.getRuntime().exec(cmd2);
                    }
                } catch (InterruptedException x) {
                    result = false;
		    RegistryBrowser.displayError(rb.
			       getString("message.error.failedToLaunchBrowser",
					 new Object []{cmd}), x);
                }
            }
        } catch (IOException ex) {
            // couldn't exec browser
            result = false;
            RegistryBrowser.displayError(rb.
			       getString("message.error.failedToLaunchBrowser",
					 new Object []{cmd}), ex);
        }

        return result;
    }

    /**
     * Try to determine whether this application is running under
     * Windows
     * or some other platform by examing the "os.name" property.
     *
     * @return true if this application is running under a Windows
     * OS
     */
    public static boolean isWindowsPlatform() {
        String os = props.getProperty("os.name");

        if ((os != null) && os.toLowerCase().startsWith(WIN_ID)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Simple example. Opens the url to "http://ebxmlrr.sourceforge.net"
     */
    public static void main(String[] args) {
        displayURL("http://ebxmlrr.sourceforge.net");
    }
}
