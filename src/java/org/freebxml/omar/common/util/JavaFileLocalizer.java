/*
 * ====================================================================
 *
 * This code is subject to the freebxml License, Version 1.1
 *
 * Copyright (c) 2001 - 2005 freebxml.org.  All rights reserved.
 *
 * $Header: /cvsroot/ebxmlrr/omar/src/java/org/freebxml/omar/common/util/JavaFileLocalizer.java,v 1.2 2006/02/08 18:38:56 farrukh_najmi Exp $
 * ====================================================================
 */
package org.freebxml.omar.common.util;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Locale;


/**
 *
 * @author Paul Sterk
 */
public class JavaFileLocalizer {
    private static JavaFileLocalizer instance = null;
    
    public synchronized static JavaFileLocalizer getInstance() {
        if (instance == null) {
            instance = new JavaFileLocalizer();
        }
        return instance;
    }

    
    private static int getLogIndex(String line) {
        int index = line.indexOf("log.info(\"");
        if (index != -1) {
            index += "log.info(\"".length();
        } else {    
            index = line.indexOf("log.warn(\"");
            if (index != -1) {
                index += "log.warn(\"".length();
            } else {
                index = line.indexOf("log.error(\"");
                if (index != -1) {
                    index += "log.error(\"".length();
                } else {
                    index = line.indexOf("log.fatal(\"");
                    if (index != -1) {
                        index += "log.fatal(\"".length();
                    }
                }
            }
        }      
        return index;
    }
    
    public static String getRbHelperClass(String filename) {
        String rbHelperClass = null;
        int index = filename.indexOf("/omar/client/ui/thin");
        if (index != -1) {
            rbHelperClass = "WebUIResourceBundle";
        } else {
            index = filename.indexOf("/omar/client/ui/swing");
            if (index != -1) {
                rbHelperClass = "JavaUIResourceBundle";
            } else {
                index = filename.indexOf("/omar/client/admin");
                if (index != -1) {
                    rbHelperClass = "AdminResourceBundle";
                } else {
                    index = filename.indexOf("/omar/client/xml/registry");
                    if (index != -1) {
                        rbHelperClass = "JAXRResourceBundle";
                    } else {
                        index = filename.indexOf("/omar/common");
                        if (index != -1) {
                            rbHelperClass = "CommonResourceBundle";
                        } else {
                            index = filename.indexOf("/omar/server");
                            if (index != -1) {
                                rbHelperClass = "ServerResourceBundle";
                            }
                        }
                    }
                }
            }
        }
        return rbHelperClass;
    }
    
    public static void main(String[] args) {
        PrintWriter newRBfile = null;
        PrintWriter newJavafile = null;
        BufferedReader in = null;
        try {           
            String rbHelperClass = null;
            String omarWorkspace = args[0];
            omarWorkspace += "/";
            System.out.println("omar workspace:" + omarWorkspace);
            String filename = args[1];
            System.out.println("java file to localize: " + filename);
            rbHelperClass = getRbHelperClass(filename);
            String fullFileName = omarWorkspace + filename;
            System.out.println("java full filename to localize: " + fullFileName);
            
            int startIndex = filename.lastIndexOf('/');
            int endIndex = filename.lastIndexOf('.');
            String fname = filename.substring(startIndex+1, endIndex);
            
            in = new BufferedReader(new FileReader(fullFileName));
            
            String rbFileName = omarWorkspace + 
                 "src/resources/ResourceBundle.log."+fname+".properties";
            System.out.println("full resource bundle file: "+rbFileName);
            //BufferedReader propin = new BufferedReader(new FileReader(propFileName));
            newRBfile = new PrintWriter(new FileOutputStream(new File(rbFileName)));
            newJavafile = new PrintWriter(new FileOutputStream(new File(fullFileName+".new")));
            String origLine = null;
            int keyNumber = 1;
            while((origLine = in.readLine()) != null) {
                int startLogMsgIndex = getLogIndex(origLine);
                // if you find a line with log.info log.error or log.fatal
                if (startLogMsgIndex != -1) {
                    int endLogMsgIndex = origLine.indexOf(';');
                    if (endLogMsgIndex == -1) {
                        endLogMsgIndex = origLine.lastIndexOf('+') + 1;
                    }
                    if (endLogMsgIndex != -1) {
                        // logging takes up just 1 line
                        boolean checkForEndMsg = false;
                        boolean checkForNextArg = false;
                        String msg = origLine.substring(startLogMsgIndex,  
                                                        endLogMsgIndex);
                        String msgPrefix = origLine.substring(0, startLogMsgIndex-1);
                        
                        char[] msgArray = msg.toCharArray();
                        char[] keyArray = new char[1024];
                        char[] iMsgArray = new char[1024];
                        char[][] argsArray = new char[10][100];
                        String iMsg = null;
                        String key = null;
                        int endMsgIndex = 0;
                        int keyIndex = 0;
                        int placeHolderIndex = -1;
                        int argCharIndex = 0;
                        boolean toUpper = false;
                        boolean newArg = true;
                        boolean workingOnArg = false;
                        int iIndex = 0;
                        int msgIndex = 0;
                        for ( ; msgIndex < msgArray.length; msgIndex++) {
                            char ch = msgArray[msgIndex];
                            if (checkForEndMsg == false) {                                
                                if (ch == '"') {
                                    // this may be the end of the message
                                    checkForEndMsg = true;
                                } else {
                                    iMsgArray[iIndex] = ch;
                                    iIndex++;
                                    // not a space, key char to the key
                                    if (ch == ' ') { 
                                        toUpper = true;
                                    } else if (ch != '.' && ch != ':') {          
                                        if (toUpper) {
                                            keyArray[keyIndex] = Character.toUpperCase(ch);
                                            toUpper = false;
                                        } else {
                                            keyArray[keyIndex] = ch;
                                        }
                                        keyIndex++;
                                    }
                                }    
                            } else {
                                // look for a ) or a + sign
                                if (checkForNextArg) {
                                    if (ch == '"') {
                                        // we have another string. stop checking
                                        // for end or next arg
                                        checkForNextArg = false;
                                        checkForEndMsg = false;
                                    } else if (ch != ',' && ch != ':' 
                                               && ch != '+') {
                                            
                                        if ((ch == ')' && iMsgArray[iIndex-1] != '(')
                                            || (ch == ' ' && workingOnArg == true)) {
                                            checkForNextArg = false;
                                            //checkForEndMsg = false;
                                            workingOnArg = false;
                                            // end of message
                                            iMsg = new String(iMsgArray);
                                            iMsg = iMsg.trim();
                                            key = new String(keyArray);
                                            key = key.trim();
                                        }
                                        // get the name of the arg
                                        else if (ch != ' ') {
                                                                                   // we have another char, but it is not
                                            // a string. It must be an argument
                                            // add placeholder to key
                                            if (newArg) {                                                                              
                                                placeHolderIndex++;
                                                newArg = false;

                                                iMsgArray[iIndex] = '{';
                                                iIndex++;
                                                iMsgArray[iIndex] = String.valueOf(placeHolderIndex).charAt(0);
                                                iIndex++;
                                                iMsgArray[iIndex] = '}';
                                                iIndex++;
                                            }                                          
                                            argsArray[placeHolderIndex][argCharIndex] = ch;
                                            argCharIndex++;
                                            workingOnArg = true;
                                        } 
                                    }// else if (ch == ' ') {
                                        // stop checking
                                     //   checkForNextArg = false;
                                    //}
                                } else {
                                    if (ch == ')' || ch == ';') {
                                        // end of message
                                        iMsg = new String(iMsgArray);
                                        iMsg = iMsg.trim();
                                        key = new String(keyArray);
                                        key = key.trim();
                                    } else if (ch == '+') {
                                        // we could have an argument or a new string
                                        checkForNextArg = true;
                                        newArg = true;
                                        argCharIndex = 0;
                                        // at end of line with a plus sign
                                        // read next line
                                        if (msgIndex == msg.length()-1) {
                                            origLine = in.readLine();
                                            endLogMsgIndex = origLine.indexOf(';');
                                            if (endLogMsgIndex != -1) {                               
                                                startLogMsgIndex = origLine.indexOf('"');
                                                if (startLogMsgIndex == -1) {
                                                    msg = origLine.trim();
                                                    checkForNextArg = true;                                                   
                                                    checkForEndMsg = true;
                                                } else {
                                                    msg = " " + origLine.substring(startLogMsgIndex+1, endLogMsgIndex);                                               
                                                    checkForNextArg = false;
                                                    checkForEndMsg = false;
                                                }
                                                msgArray = msg.toCharArray();
                                                msgIndex = 0;
                                            }
                                        }
                                    }
                                }
                            }                                                       
                            endMsgIndex = msgIndex;
                        }
                        int commaIndex = origLine.indexOf(',');
                        String msgSuffix = null;
                        if (commaIndex > -1) {
                            msgSuffix = origLine.substring(commaIndex);
                        } else {
                            msgSuffix = origLine.substring(startLogMsgIndex+endMsgIndex);
                        }
                     
                        String newLine = msgPrefix + rbHelperClass +
                            ".getInstance().getString(\"message." + key;
                        if (placeHolderIndex > -1) {
                            newLine += "\", new Object[]{";
                            for (int j = 0; j <= placeHolderIndex; j++) {
                                if (j > 0) {
                                    newLine += ", ";
                                }
                                String arg = new String(argsArray[j]);
                                arg = arg.trim();
                                newLine += arg;
                            }
                            newLine += "})";
                            placeHolderIndex = -1;
                            newLine += msgSuffix;
                        } else {
                            newLine += "\")" + msgSuffix;
                        }
                        newRBfile.println("message." + key + "=" + iMsg);
                        newJavafile.println(newLine);

                    } else {
                        // logging takes up > 1 line
                    }
                } else {
                    newJavafile.println(origLine);
                }
            }
        } catch (Exception e){
            System.out.println(e);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {}
            try {
                newJavafile.close();
            } catch (Throwable ex) {}
            try {
                newRBfile.close();
            } catch (Throwable ex) {}
        }
    }
    
    
    static class LocalizerClassLoader extends ClassLoader {
        protected URL findResource(String name) {
            URL resourceUrl = null;
            try {
                resourceUrl = new URL("file", "localhost", name);
            } catch (Exception ex) {
                
            }
            return resourceUrl;
        }
    }
}
