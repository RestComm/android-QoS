/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * For questions related to commercial use licensing, please contact sales@telestax.com.
 *
 */

package org.restcomm.app.utillib.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import android.util.Log;


/**
 * This class is a replacement for the Log static class. The advantage of using this class
 * is the the TAG that is used for the log statements is the same for all the classes, but the 
 * class name is mentioned in the message. This means that filtering ACRA
 * to send the logcat for this application only is very simple. The class name is still maintained
 * in the message.
 * @author Abhin
 *
 */
public class LoggerUtil {
    /**
     * Path of file that {@link LoggerUtil#logToFile} write to.
     */
    public static final String LOG_FILE = "/sdcard/mmclog.txt";

    public static final String LOG_TRANSIT_FILE = "/sdcard/mmclogtransit.txt";

    public static final String TAG = "com.cortxt.app.MMC";

    /**
     * Flag to hold whether the application is debug mode or not
     */
    public static boolean DEBUGGABLE = false;

    /**
     * Sets {@link LoggerUtil#DEBUGGABLE} to debug
     * @param debug
     */
    public static void setDebuggable (boolean debug) {
        LoggerUtil.DEBUGGABLE = debug;
    }

    /**
     * @return {@link LoggerUtil#DEBUGGABLE}
     */
    public static boolean isDebuggable() {
        return LoggerUtil.DEBUGGABLE;
    }

    private static Object logFileLock = new Object();
    /**
     * Logs to a file.
     * This is needed because the android log clears itself.
     * @param level
     * @param className
     * @param methodName
     * @param message
     */
    public static void logToFile(Level level, String className, String methodName, String message) {
        Date d = new Date(System.currentTimeMillis());
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String timestamp = dateFormat.format(d);

        String logText = timestamp + " - " + level + " - " +  className + " - " + methodName + " - " + message + "\n";

        writeToFile(logText, LOG_FILE);
        if(isDebuggable())
            Log.i(className, methodName + " " + message);
    }

    public static void logToTransitFile(Level level, String className, String methodName, String message) {
        Date d = new Date(System.currentTimeMillis());
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String timestamp = dateFormat.format(d);

        String logText = timestamp + " - " + level + " - " +  className + " - " + methodName + " - " + message + "\n";

        writeToFile(logText, LOG_TRANSIT_FILE);
        if(isDebuggable())
            Log.i(className, methodName + " " + message);
    }

    /**
     * Logs to a file.
     * @param level
     * @param className
     * @param methodName
     * @param message
     * @param e
     */
    public static void logToFile(Level level, String className, String methodName, String message, Exception e) {
        String stackTrace = Global.getStackTrace ( e);
//        String stackTrace = "\n\t" + e.toString();
//        StackTraceElement[] stackTraceElements = e.getStackTrace();
//        int len = stackTraceElements.length;
//        if (len > 3)
//            len = 3;
//        for(int i=0; i<len; i++) {
//            stackTrace += "\n\t" + stackTraceElements[i].getClassName() + "." + stackTraceElements[i].getMethodName() + " (" + stackTraceElements[i].getFileName() + " : " + stackTraceElements[i].getLineNumber() + ")";
//        }

        logToFile(level, className, methodName, message + stackTrace);
    }

    /**
     * Temporary method for logging events and samples to separate file
     * @param message
     */
    public static void logToOtherFile(String message) {
        writeToFile(message, "/sdcard/events.txt");
    }

    private static void writeToFile(String text, String filePath) {
        if(isDebuggable()) {
            synchronized(logFileLock) {
                try {
                    File file = new File(filePath);
                    if(!file.exists()) {
                        file.createNewFile();
                    }
                    FileWriter fileWriter = new FileWriter(file, true);
                    BufferedWriter outputStream = new BufferedWriter(fileWriter);

                    try {
                        outputStream.append(text);
                    }
                    catch (IOException ioe_writingToFile) {
                        //Log.e("MMCLogger", "error writing to log file " + ioe_writingToFile.getMessage(), null);
                    }
                    finally {
                        outputStream.close();
                        fileWriter.close();
                    }
                }
                catch (IOException ioe_openingFile) {
                    //Log.e("MMCLogger", "error opening log file " + ioe_openingFile.getMessage(), null);
                }
            }
        }
    }

    public enum Level {
        DEBUG {
            public String toString() {
                return "DEBUG";
            }
        },
        WARNING{
            public String toString() {
                return "WARNING";
            }
        },
        ERROR {
            public String toString() {
                return "ERROR";
            }
        },
        WTF {
            public String toString() {
                return "WTF";
            }
        }

    }
}