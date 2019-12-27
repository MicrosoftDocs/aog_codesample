/*
 * Copyright (c) 2015-2020, Chen Rui
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vianet.azure.sdk.manage.utils;

import java.io.*;
import java.util.Date;

/**
 * JAVA I/O Operation Tools
 *
 * @author Chen Rui
 */
public class IOUtils {

    /**
     * read string from input stream
     *
     * @param inputStream Input stream
     * @return String input stream content
     * @throws IOException
     */
    public static String slurp(final InputStream inputStream) throws IOException {
        final char[] buffer = new char[2048];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, "UTF-8");
        for (;;) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }

    /**
     * Close I/O and closeable object quietly
     *
     * @param closeable closeable object
     */
    public static void closeQuietly(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static File createTmpFile() throws IOException {
    	String tmpName = String.valueOf(new Date().getTime());
    	File temp = File.createTempFile(tmpName, ".suffix");
    	return temp;
    }

}
